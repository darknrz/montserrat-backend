package com.monserrat.service;

import com.monserrat.dto.academico.TallerCatalogoDTO;
import com.monserrat.dto.academico.TallerCatalogoRequest;
import com.monserrat.dto.academico.TallerDTO;
import com.monserrat.dto.academico.TallerPagoRequest;
import com.monserrat.entity.RolUsuario;
import com.monserrat.entity.Taller;
import com.monserrat.entity.TallerCatalogo;
import com.monserrat.entity.UsuarioAcademico;
import com.monserrat.repository.TallerCatalogoRepository;
import com.monserrat.repository.TallerRepository;
import com.monserrat.repository.UsuarioAcademicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

/**
 * Talleres fijos (catalogo) asignados automaticamente por salon/grado.
 * Los registros por alumno (tabla talleres) guardan el monto cancelado.
 */
@Service
@RequiredArgsConstructor
public class TallerService {

    private final TallerCatalogoRepository catalogoRepository;
    private final TallerRepository tallerRepository;
    private final UsuarioAcademicoRepository usuarioRepository;

    // ---------- Catalogo ----------

    @Transactional
    public List<TallerCatalogoDTO> listarCatalogo(Integer anio) {
        migrarRegistrosAntiguos(anio);
        return catalogoRepository.findByAnioOrderByNombreAsc(anio).stream().map(this::toDto).toList();
    }

    @Transactional
    public TallerCatalogoDTO crear(TallerCatalogoRequest request) {
        TallerCatalogo catalogo = TallerCatalogo.builder().anio(request.getAnio()).build();
        aplicar(catalogo, request);
        return toDto(catalogoRepository.save(catalogo));
    }

    @Transactional
    public TallerCatalogoDTO actualizar(Long id, TallerCatalogoRequest request) {
        TallerCatalogo catalogo = buscar(id);
        aplicar(catalogo, request);
        TallerCatalogo saved = catalogoRepository.save(catalogo);
        // Mantener alineado el monto adeudado de los registros ya creados.
        for (Taller t : tallerRepository.findByCatalogo_Id(id)) {
            t.setNombre(saved.getNombre());
            t.setMonto(saved.getMonto());
            t.setAnio(saved.getAnio());
            t.setPagada(estaPagado(t.getMontoPagado(), saved.getMonto(), t.getPagada()));
            tallerRepository.save(t);
        }
        return toDto(saved);
    }

    @Transactional
    public void eliminar(Long id) {
        TallerCatalogo catalogo = buscar(id);
        tallerRepository.deleteAll(tallerRepository.findByCatalogo_Id(id));
        catalogoRepository.delete(catalogo);
    }

    // ---------- Pagos por alumno ----------

    @Transactional
    public TallerDTO registrarPago(TallerPagoRequest request) {
        TallerCatalogo catalogo = buscar(request.getCatalogoId());
        UsuarioAcademico alumno = usuarioRepository.findByDni(request.getAlumnoDni())
                .filter(u -> RolUsuario.ALUMNO.equals(u.getRol()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alumno no encontrado"));
        if (request.getMontoPagado() != null && request.getMontoPagado().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto no puede ser negativo");
        }
        Taller taller = tallerRepository.findByAlumno_DniAndCatalogo_Id(alumno.getDni(), catalogo.getId())
                .orElseGet(() -> Taller.builder().alumno(alumno).catalogo(catalogo).build());
        taller.setAnio(catalogo.getAnio());
        taller.setNombre(catalogo.getNombre());
        taller.setMonto(catalogo.getMonto());
        BigDecimal pagado = request.getMontoPagado();
        if (pagado == null && request.getPagada() != null) {
            pagado = Boolean.TRUE.equals(request.getPagada()) ? catalogo.getMonto() : BigDecimal.ZERO;
        }
        taller.setMontoPagado(pagado);
        taller.setPagada(estaPagado(pagado, catalogo.getMonto(), false));
        if (request.getObservacion() != null) taller.setObservacion(request.getObservacion());
        return toTallerDto(tallerRepository.save(taller));
    }

    // Talleres del alumno logueado: registros propios + talleres del catalogo que le aplican por salon/grado.
    @Transactional
    public List<TallerDTO> listarParaAlumno(String alumnoDni) {
        UsuarioAcademico alumno = usuarioRepository.findByDni(alumnoDni)
                .filter(u -> RolUsuario.ALUMNO.equals(u.getRol()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        List<TallerDTO> result = new ArrayList<>();
        Set<Long> cubiertos = new HashSet<>();
        for (Taller t : tallerRepository.findByAlumno_DniOrderByAnioDesc(alumnoDni)) {
            result.add(toTallerDto(t));
            if (t.getCatalogo() != null) cubiertos.add(t.getCatalogo().getId());
        }
        for (TallerCatalogo c : catalogoRepository.findAll()) {
            if (cubiertos.contains(c.getId()) || !aplica(c, alumno)) continue;
            result.add(TallerDTO.builder()
                    .id(-c.getId())
                    .catalogoId(c.getId())
                    .alumnoDni(alumno.getDni())
                    .alumnoCodigo(alumno.getCodigo())
                    .alumnoNombre(alumno.getNombre())
                    .nivelEducativo(alumno.getNivelEducativo())
                    .grado(alumno.getGrado())
                    .seccion(alumno.getSeccion())
                    .anio(c.getAnio())
                    .nombre(c.getNombre())
                    .monto(c.getMonto())
                    .montoPagado(BigDecimal.ZERO)
                    .pagada(false)
                    .build());
        }
        result.sort(Comparator.comparing(TallerDTO::getAnio).reversed());
        return result;
    }

    // ---------- Internos ----------

    private void aplicar(TallerCatalogo catalogo, TallerCatalogoRequest request) {
        if (request.getMonto() == null || request.getMonto().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto no puede ser negativo");
        }
        catalogo.setAnio(request.getAnio());
        catalogo.setNombre(request.getNombre().trim());
        catalogo.setMonto(request.getMonto());
        Set<String> destinos = new LinkedHashSet<>();
        if (request.getAplicaA() != null) {
            for (String d : request.getAplicaA()) {
                if (d != null && (d.startsWith("SALON:") || d.startsWith("GRADO:")) && d.length() > 6) destinos.add(d);
            }
        }
        catalogo.getAplicaA().clear();
        catalogo.getAplicaA().addAll(destinos);
    }

    private TallerCatalogo buscar(Long id) {
        return catalogoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Taller no encontrado"));
    }

    private boolean estaPagado(BigDecimal pagado, BigDecimal monto, Boolean actual) {
        if (pagado == null) return Boolean.TRUE.equals(actual);
        return pagado.signum() > 0 && pagado.compareTo(monto) >= 0;
    }

    // Agrupa los registros antiguos (por alumno, sin catalogo) en talleres fijos por nombre.
    private void migrarRegistrosAntiguos(Integer anio) {
        List<Taller> huerfanos = tallerRepository.findByCatalogoIsNullAndAnio(anio);
        if (huerfanos.isEmpty()) return;
        Map<String, TallerCatalogo> porNombre = new HashMap<>();
        for (TallerCatalogo c : catalogoRepository.findByAnioOrderByNombreAsc(anio)) {
            porNombre.put(c.getNombre().trim().toLowerCase(), c);
        }
        for (Taller t : huerfanos) {
            String key = t.getNombre().trim().toLowerCase();
            TallerCatalogo c = porNombre.computeIfAbsent(key, k -> catalogoRepository.save(TallerCatalogo.builder()
                    .anio(anio).nombre(t.getNombre().trim()).monto(t.getMonto()).build()));
            t.setCatalogo(c);
            if (t.getMontoPagado() == null && Boolean.TRUE.equals(t.getPagada())) t.setMontoPagado(t.getMonto());
            tallerRepository.save(t);
        }
    }

    static String salonDe(UsuarioAcademico a) {
        String seccion = a.getSeccion() == null ? "" : a.getSeccion().name();
        switch (seccion) {
            case "CICLADO_I": return "CICLADO I";
            case "CICLADO_II": return "CICLADO II";
            case "ANUAL": return "ANUAL";
            case "LETRAS": return "LETRAS";
            case "CIENCIAS": return "CIENCIAS";
            default: break;
        }
        String grado = a.getGrado() == null ? "" : a.getGrado().name();
        switch (grado) {
            case "INICIAL": return "INICIAL";
            case "PRIMERO_PRIMARIA": return "PRIMERO PRIMARIA";
            case "SEGUNDO_PRIMARIA": return "SEGUNDO PRIMARIA";
            case "TERCERO_PRIMARIA": return "TERCERO PRIMARIA";
            case "CUARTO_PRIMARIA": return "CUARTO PRIMARIA";
            case "QUINTO_PRIMARIA": return "PRE FORMATIVO";
            default: return "";
        }
    }

    private boolean aplica(TallerCatalogo c, UsuarioAcademico a) {
        String salon = salonDe(a);
        if (!salon.isEmpty() && c.getAplicaA().contains("SALON:" + salon)) return true;
        return a.getGrado() != null && c.getAplicaA().contains("GRADO:" + a.getGrado().name());
    }

    private TallerCatalogoDTO toDto(TallerCatalogo c) {
        return TallerCatalogoDTO.builder()
                .id(c.getId()).anio(c.getAnio()).nombre(c.getNombre()).monto(c.getMonto())
                .aplicaA(new ArrayList<>(c.getAplicaA()))
                .build();
    }

    private TallerDTO toTallerDto(Taller taller) {
        UsuarioAcademico alumno = taller.getAlumno();
        return TallerDTO.builder()
                .id(taller.getId())
                .catalogoId(taller.getCatalogo() == null ? null : taller.getCatalogo().getId())
                .montoPagado(taller.getMontoPagado())
                .alumnoDni(alumno.getDni())
                .alumnoCodigo(alumno.getCodigo())
                .alumnoNombre(alumno.getNombre())
                .nivelEducativo(alumno.getNivelEducativo())
                .grado(alumno.getGrado())
                .seccion(alumno.getSeccion())
                .anio(taller.getAnio())
                .nombre(taller.getNombre())
                .monto(taller.getMonto())
                .pagada(Boolean.TRUE.equals(taller.getPagada()))
                .observacion(taller.getObservacion())
                .creadoEn(taller.getCreatedAt())
                .actualizadoEn(taller.getUpdatedAt())
                .build();
    }
}
