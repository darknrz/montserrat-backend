package com.monserrat.config;

import com.monserrat.entity.Admin;
import com.monserrat.entity.AsignacionAcademica;
import com.monserrat.entity.CatalogoAcademico;
import com.monserrat.entity.ChatbotFaq;
import com.monserrat.entity.Ingreso;
import com.monserrat.entity.Institution;
import com.monserrat.entity.RedSocial;
import com.monserrat.entity.RolUsuario;
import com.monserrat.entity.UsuarioAcademico;
import com.monserrat.entity.Video;
import com.monserrat.repository.AdminRepository;
import com.monserrat.repository.AsignacionAcademicaRepository;
import com.monserrat.repository.CatalogoAcademicoRepository;
import com.monserrat.repository.ChatbotFaqRepository;
import com.monserrat.repository.IngresoRepository;
import com.monserrat.repository.InstitutionRepository;
import com.monserrat.repository.RedSocialRepository;
import com.monserrat.repository.UsuarioAcademicoRepository;
import com.monserrat.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    private String serializeDocentes(List<String> docentes) {
        return docentes == null ? "" : docentes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.joining(","));
    }

    @Bean
    public CommandLineRunner initData(
            AdminRepository adminRepo,
            InstitutionRepository institutionRepo,
            IngresoRepository ingresoRepo,
            VideoRepository videoRepo,
            RedSocialRepository redSocialRepo,
            UsuarioAcademicoRepository usuarioAcademicoRepo,
            AsignacionAcademicaRepository asignacionRepo,
            ChatbotFaqRepository chatbotFaqRepo,
            CatalogoAcademicoRepository catalogoRepo) {

        return args -> {
            if (adminPassword != null && !adminPassword.isBlank()) {
                adminRepo.findByUsername(adminUsername).ifPresentOrElse(admin -> {
                    admin.setPassword(passwordEncoder.encode(adminPassword));
                    admin.setNombre("Administrador Monserrat");
                    admin.setRol("ADMIN");
                    admin.setActivo(true);
                    adminRepo.save(admin);
                    log.info("Admin sincronizado para {}", adminUsername);
                }, () -> {
                    adminRepo.save(Admin.builder()
                            .username(adminUsername)
                            .password(passwordEncoder.encode(adminPassword))
                            .nombre("Administrador Monserrat")
                            .rol("ADMIN")
                            .activo(true)
                            .build());
                    log.info("Admin inicial creado para {}", adminUsername);
                });
            } else if (adminRepo.count() == 0) {
                adminRepo.save(Admin.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("change-me"))
                        .nombre("Administrador Monserrat")
                        .rol("ADMIN")
                        .activo(true)
                        .build());
                log.warn("Se creo un admin temporal. Define app.admin.password o APP_ADMIN_PASSWORD antes de produccion.");
            }

            if (institutionRepo.count() == 0) {
                institutionRepo.save(Institution.builder()
                        .nombre("I.E.P. Nuestra Senora de Monserrat")
                        .direccion("Jr. Cajamarca #563")
                        .ciudad("Huancayo")
                        .distrito("Huancayo")
                        .anioFundacion("2007")
                        .telefono("064-000000")
                        .email("monserratcomplejoeducativo@gmail.com")
                        .niveles("Primaria y Secundaria")
                        .tipo("Institucion Educativa Privada")
                        .mision("Brindar una educacion integral de calidad, formando estudiantes con valores solidos, pensamiento critico y vocacion de servicio a la comunidad de Huancayo.")
                        .vision("Ser la institucion educativa privada lider en Huancayo, reconocida por la excelencia academica, valores morales y el ingreso de nuestros egresados a las mejores universidades del pais.")
                        .descripcion("La Institucion Educativa Privada Nuestra Senora de Monserrat tuvo inicio en diciembre del 2007 en Huancayo, Peru.")
                        .horarioAtencion("Lun-Vie 7:30am-5:00pm")
                        .build());
                log.info("Datos institucionales cargados");
            }

            if (ingresoRepo.count() == 0) {
                List<Ingreso> ingresantes = List.of(
                        Ingreso.builder().nombre("Yupanqui Morales Vladimir Fernan").universidad("Universidad Nacional del Centro del Peru").universidadSiglas("UNCP").carrera("Sociologia").anio("2025").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Carrion Armaulia Samael").universidad("Universidad Nacional del Centro del Peru").universidadSiglas("UNCP").carrera("Sociologia").anio("2025").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Garcia Marmolejo Franco Ivan").universidad("Universidad Nacional del Centro del Peru").universidadSiglas("UNCP").carrera("Matematica e Informatica").anio("2025").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Magarino Vega Patrick Randolf").universidad("Universidad Nacional del Centro del Peru").universidadSiglas("UNCP").carrera("Ingenieria Mecanica").anio("2025").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Basurto Rojas Tiago Mateus").universidad("Universidad Nacional del Centro del Peru").universidadSiglas("UNCP").carrera("Ingenieria Mecanica Electrica").anio("2025").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Flores Torres Adriana").universidad("Universidad Nacional Mayor de San Marcos").universidadSiglas("UNMSM").carrera("Medicina Humana").anio("2025").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Quispe Torres Ana Lucia").universidad("Universidad Nacional del Centro del Peru").universidadSiglas("UNCP").carrera("Contabilidad").anio("2024").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Flores Huanca Jorge Luis").universidad("Universidad Nacional del Centro del Peru").universidadSiglas("UNCP").carrera("Administracion").anio("2024").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Lazo Palomino Kevin").universidad("Universidad Nacional de Ingenieria").universidadSiglas("UNI").carrera("Ingenieria Civil").anio("2024").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Huaman Ccente Rosa").universidad("Universidad Peruana Los Andes").universidadSiglas("UPLA").carrera("Derecho").anio("2024").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Poma Asto Ricardo").universidad("Universidad Nacional Federico Villarreal").universidadSiglas("UNFV").carrera("Odontologia").anio("2024").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Soto Lozano Milagros").universidad("Universidad San Martin de Porres").universidadSiglas("USMP").carrera("Arquitectura").anio("2024").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Ramos Ccente Valeria").universidad("Universidad Nacional del Centro del Peru").universidadSiglas("UNCP").carrera("Ingenieria de Sistemas").anio("2023").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Condori Poma Milagros").universidad("Universidad Nacional Mayor de San Marcos").universidadSiglas("UNMSM").carrera("Enfermeria").anio("2023").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Torres Solis Daniela").universidad("Universidad Peruana Los Andes").universidadSiglas("UPLA").carrera("Psicologia").anio("2023").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Huaman Asto Brayan").universidad("Universidad Nacional Agraria La Molina").universidadSiglas("UNALM").carrera("Agronomia").anio("2023").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Castillo Rojas Jean Paul").universidad("Universidad Nacional de Huancavelica").universidadSiglas("UNH").carrera("Educacion").anio("2022").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Mendoza Cruz Fiorella").universidad("Universidad Nacional del Centro del Peru").universidadSiglas("UNCP").carrera("Derecho").anio("2022").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Vargas Lima Luis").universidad("Universidad Nacional de Ingenieria").universidadSiglas("UNI").carrera("Ingenieria de Sistemas").anio("2022").tipoSeleccion("1ra Seleccion").activo(true).build(),
                        Ingreso.builder().nombre("Apaza Rios Carmen").universidad("Universidad Nacional Federico Villarreal").universidadSiglas("UNFV").carrera("Trabajo Social").anio("2022").tipoSeleccion("1ra Seleccion").activo(true).build()
                );
                ingresoRepo.saveAll(ingresantes);
                log.info("{} ingresantes cargados", ingresantes.size());
            }

            if (videoRepo.count() == 0) {
                log.info("No se cargaron medios iniciales. El carrusel quedara listo para que el admin suba archivos a Cloudinary.");
            }

            if (redSocialRepo.count() == 0) {
                List<RedSocial> redes = List.of(
                        RedSocial.builder().nombre("Facebook").icono("facebook").url("https://www.facebook.com/IEMonserratHuancayo").activo(true).orden(1).build(),
                        RedSocial.builder().nombre("TikTok").icono("tiktok").url("https://www.tiktok.com/@monserrathuancayo").activo(true).orden(2).build(),
                        RedSocial.builder().nombre("YouTube").icono("youtube").url("https://www.youtube.com/@monserrathuancayo").activo(true).orden(3).build(),
                        RedSocial.builder().nombre("Instagram").icono("instagram").url("https://www.instagram.com/monserrathuancayo").activo(true).orden(4).build()
                );
                redSocialRepo.saveAll(redes);
                log.info("{} redes sociales cargadas", redes.size());
            }

            if (chatbotFaqRepo.count() == 0) {
                List<ChatbotFaq> faqs = List.of(
                        ChatbotFaq.builder().pregunta("Como puedo consultar sobre matricula").respuesta("Para informacion de matricula, escriba a monserratcomplejoeducativo@gmail.com o visitenos en Jr. Cajamarca #563, Huancayo.").categoria("Matricula").orden(1).activo(true).build(),
                        ChatbotFaq.builder().pregunta("Cual es el horario de atencion").respuesta("El horario de atencion es Lun-Vie 7:30am-5:00pm.").categoria("Atencion").orden(2).activo(true).build(),
                        ChatbotFaq.builder().pregunta("Donde esta ubicada la institucion").respuesta("La institucion esta ubicada en Jr. Cajamarca #563, Huancayo.").categoria("Ubicacion").orden(3).activo(true).build(),
                        ChatbotFaq.builder().pregunta("Que niveles educativos ofrece").respuesta("La institucion ofrece los niveles de Primaria y Secundaria.").categoria("Academico").orden(4).activo(true).build(),
                        ChatbotFaq.builder().pregunta("Cuanto cuesta la pension").respuesta("Para informacion sobre pensiones o costos, comunicate directamente al correo monserratcomplejoeducativo@gmail.com.").categoria("Costos").orden(5).activo(true).build()
                );
                chatbotFaqRepo.saveAll(faqs);
                log.info("{} preguntas frecuentes del chatbot cargadas", faqs.size());
            }

            // Crear docentes de PRIMARIA
            List<UsuarioAcademico> docentesPrimaria = List.of(
                    UsuarioAcademico.builder().dni("10000001").password(passwordEncoder.encode("10000001")).nombre("Miss Daniela").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.PRIMARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("10000002").password(passwordEncoder.encode("10000002")).nombre("Miss Leslie").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.PRIMARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("10000003").password(passwordEncoder.encode("10000003")).nombre("Mirian Diego").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.PRIMARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("10000004").password(passwordEncoder.encode("10000004")).nombre("Miss Karin").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.PRIMARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("10000005").password(passwordEncoder.encode("10000005")).nombre("Rosvita Gómez").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.PRIMARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("10000006").password(passwordEncoder.encode("10000006")).nombre("Prof. Odilio").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.PRIMARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("10000007").password(passwordEncoder.encode("10000007")).nombre("Omar").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.PRIMARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("10000008").password(passwordEncoder.encode("10000008")).nombre("Prof. Cristian Bonifacio").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.PRIMARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("10000009").password(passwordEncoder.encode("10000009")).nombre("Miriam Marcelo").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.PRIMARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("10000010").password(passwordEncoder.encode("10000010")).nombre("Miss Adaluz").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.PRIMARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("10000011").password(passwordEncoder.encode("10000011")).nombre("Miss Lourdes").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.PRIMARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("10000012").password(passwordEncoder.encode("10000012")).nombre("Prof. Christian Maga").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.PRIMARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("10000013").password(passwordEncoder.encode("10000013")).nombre("Omar Bruno").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.PRIMARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build()
            );
            for (UsuarioAcademico docente : docentesPrimaria) {
                if (!usuarioAcademicoRepo.existsByDni(docente.getDni())) {
                    usuarioAcademicoRepo.save(docente);
                }
            }
            log.info("{} docentes de primaria creados/verificados", docentesPrimaria.size());

            // Asignar códigos secuenciales a docentes de PRIMARIA (solo a los que no tienen código aún)
            List<UsuarioAcademico> docentesPrimariaActuales = usuarioAcademicoRepo.findAll().stream()
                    .filter(u -> RolUsuario.DOCENTE.equals(u.getRol()) &&
                            com.monserrat.entity.NivelEducativo.PRIMARIA.equals(u.getNivelEducativo()))
                    .sorted(Comparator.comparing(UsuarioAcademico::getDni))
                    .toList();
            int numeroPrimaria = 1;
            for (UsuarioAcademico docente : docentesPrimariaActuales) {
                if (docente.getCodigo() == null || docente.getCodigo().isBlank()) {
                    docente.setCodigo(String.format("DOC%03d", numeroPrimaria));
                    usuarioAcademicoRepo.save(docente);
                }
                numeroPrimaria++;
            }
            log.info("Códigos asignados a {} docentes de primaria", docentesPrimariaActuales.size());

            // Crear docentes de SECUNDARIA
            List<UsuarioAcademico> docentesSecundaria = List.of(
                    UsuarioAcademico.builder().dni("20000001").password(passwordEncoder.encode("20000001")).nombre("Adaluz Paye").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.SECUNDARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("20000002").password(passwordEncoder.encode("20000002")).nombre("Rosvita Gómez").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.SECUNDARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("20000003").password(passwordEncoder.encode("20000003")).nombre("Miriam Marcelo").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.SECUNDARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("20000004").password(passwordEncoder.encode("20000004")).nombre("Lourdes Bonilla").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.SECUNDARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("20000005").password(passwordEncoder.encode("20000005")).nombre("Daniela Ydrogo").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.SECUNDARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("20000006").password(passwordEncoder.encode("20000006")).nombre("Omar Bruno").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.SECUNDARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("20000007").password(passwordEncoder.encode("20000007")).nombre("Christian Magariño").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.SECUNDARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("20000008").password(passwordEncoder.encode("20000008")).nombre("Eladio Magariño").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.SECUNDARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("20000009").password(passwordEncoder.encode("20000009")).nombre("Jhonatan Carhuancho").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.SECUNDARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("20000010").password(passwordEncoder.encode("20000010")).nombre("Fernando Jacinto").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.SECUNDARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("20000011").password(passwordEncoder.encode("20000011")).nombre("Zenon Meza").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.SECUNDARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build(),
                    UsuarioAcademico.builder().dni("20000012").password(passwordEncoder.encode("20000012")).nombre("César Veliz").rol(RolUsuario.DOCENTE).nivelEducativo(com.monserrat.entity.NivelEducativo.SECUNDARIA).estado(com.monserrat.entity.EstadoUsuario.ACTIVO).activo(true).debeCambiarContrasena(true).build()
            );
            for (UsuarioAcademico docente : docentesSecundaria) {
                if (!usuarioAcademicoRepo.existsByDni(docente.getDni())) {
                    usuarioAcademicoRepo.save(docente);
                }
            }
            log.info("{} docentes de secundaria creados/verificados", docentesSecundaria.size());

            // Asignar códigos secuenciales a docentes de SECUNDARIA
            List<UsuarioAcademico> docentesSecundariaActuales = usuarioAcademicoRepo.findAll().stream()
                    .filter(u -> RolUsuario.DOCENTE.equals(u.getRol()) &&
                            com.monserrat.entity.NivelEducativo.SECUNDARIA.equals(u.getNivelEducativo()))
                    .toList();
            int docSecStart = 14; // Comenzar desde DOC014 después de docentes primaria
            for (int i = 0; i < docentesSecundariaActuales.size(); i++) {
                UsuarioAcademico docente = docentesSecundariaActuales.get(i);
                docente.setCodigo(String.format("DOC%03d", docSecStart + i));
                usuarioAcademicoRepo.save(docente);
            }
            log.info("Códigos asignados a {} docentes de secundaria", docentesSecundariaActuales.size());

            // Crear áreas curriculares y competencias de PRIMARIA
            long countCompetenciasPrimaria = catalogoRepo.findAll().stream()
                    .filter(c -> "COMPETENCIA".equals(c.getTipo()) && "PRIMARIA".equals(c.getNivel()))
                    .count();
            boolean docentesCompetenciasPrimExisten = catalogoRepo.findAll().stream()
                    .anyMatch(c -> "DOCENTE_COMPETENCIA".equals(c.getTipo()) && "PRIMARIA".equals(c.getNivel()));
            boolean tieneMapeosMatematicaPrimaria = catalogoRepo.findAll().stream()
                    .anyMatch(c -> "DOCENTE_COMPETENCIA".equals(c.getTipo())
                            && "PRIMARIA".equals(c.getNivel())
                            && c.getCodigo() != null
                            && c.getCodigo().contains("||MATEMATICA||"));

            if (countCompetenciasPrimaria != 30 || !docentesCompetenciasPrimExisten || !tieneMapeosMatematicaPrimaria) {
                log.info("Recreando áreas curriculares, competencias y docentes por competencia de PRIMARIA...");
                List<CatalogoAcademico> aEliminar = catalogoRepo.findAll().stream()
                        .filter(c -> "PRIMARIA".equals(c.getNivel()) && 
                                ("AREA_CURRICULAR".equals(c.getTipo()) || 
                                 "COMPETENCIA".equals(c.getTipo()) || 
                                 "DOCENTE_COMPETENCIA".equals(c.getTipo()) || 
                                 "COMPETENCIA_CURSO".equals(c.getTipo())))
                        .toList();
                if (!aEliminar.isEmpty()) {
                    catalogoRepo.deleteAll(aEliminar);
                }

                // Áreas Curriculares de PRIMARIA
                List<CatalogoAcademico> areasCurriculares = List.of(
                        CatalogoAcademico.builder().tipo("AREA_CURRICULAR").nivel("PRIMARIA").codigo("INGLES").nombre("Inglés").activo(true).orden(1).build(),
                        CatalogoAcademico.builder().tipo("AREA_CURRICULAR").nivel("PRIMARIA").codigo("PERSONAL_SOCIAL").nombre("Personal Social").activo(true).orden(2).build(),
                        CatalogoAcademico.builder().tipo("AREA_CURRICULAR").nivel("PRIMARIA").codigo("EDUCACION_RELIGIOSA").nombre("Educación Religiosa").activo(true).orden(3).build(),
                        CatalogoAcademico.builder().tipo("AREA_CURRICULAR").nivel("PRIMARIA").codigo("EDUCACION_FISICA").nombre("Educación Física").activo(true).orden(4).build(),
                        CatalogoAcademico.builder().tipo("AREA_CURRICULAR").nivel("PRIMARIA").codigo("COMUNICACION").nombre("Comunicación").activo(true).orden(5).build(),
                        CatalogoAcademico.builder().tipo("AREA_CURRICULAR").nivel("PRIMARIA").codigo("ARTE_CULTURA").nombre("Arte y Cultura").activo(true).orden(6).build(),
                        CatalogoAcademico.builder().tipo("AREA_CURRICULAR").nivel("PRIMARIA").codigo("CASTELLANO_SEGUNDA_LENGUA").nombre("Castellano como Segunda Lengua").activo(true).orden(7).build(),
                        CatalogoAcademico.builder().tipo("AREA_CURRICULAR").nivel("PRIMARIA").codigo("MATEMATICA").nombre("Matemática").activo(true).orden(8).build(),
                        CatalogoAcademico.builder().tipo("AREA_CURRICULAR").nivel("PRIMARIA").codigo("CIENCIA_TECNOLOGIA").nombre("Ciencia y Tecnología").activo(true).orden(9).build(),
                        CatalogoAcademico.builder().tipo("AREA_CURRICULAR").nivel("PRIMARIA").codigo("COMPETENCIAS_TRANSVERSALES").nombre("Competencias Transversales").activo(true).orden(10).build()
                );
                catalogoRepo.saveAll(areasCurriculares);
                log.info("{} áreas curriculares de primaria creadas", areasCurriculares.size());

                // Competencias de PRIMARIA
                List<CatalogoAcademico> competenciasPrimaria = List.of(
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C1").nombre("Construye su identidad.").activo(true).orden(1).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C2").nombre("Convive y participa democráticamente en la búsqueda del bien común.").activo(true).orden(2).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C3").nombre("Construye interpretaciones históricas.").activo(true).orden(3).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C4").nombre("Gestiona responsablemente el espacio y el ambiente.").activo(true).orden(4).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C5").nombre("Gestiona responsablemente los recursos económicos.").activo(true).orden(5).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C6").nombre("Construye su identidad como persona humana, amada por Dios, digna, libre y trascendente, comprendiendo la doctrina de su propia religión y abierta al diálogo con las que le son cercanas.").activo(true).orden(6).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C7").nombre("Asume la experiencia del encuentro personal y comunitario con Dios en su proyecto de vida, en coherencia con su creencia religiosa.").activo(true).orden(7).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C8").nombre("Se desenvuelve de manera autónoma a través de su motricidad.").activo(true).orden(8).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C9").nombre("Asume una vida saludable.").activo(true).orden(9).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C10").nombre("Interactúa a través de sus habilidades sociomotrices.").activo(true).orden(10).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C11").nombre("Se comunica oralmente en su lengua materna.").activo(true).orden(11).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C12").nombre("Lee diversos tipos de textos escritos.").activo(true).orden(12).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C13").nombre("Escribe diversos tipos de textos.").activo(true).orden(13).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C14").nombre("Aprecia de manera crítica manifestaciones artístico-culturales.").activo(true).orden(14).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C15").nombre("Crea proyectos desde los lenguajes artísticos.").activo(true).orden(15).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C16").nombre("Se comunica oralmente en castellano como segunda lengua.").activo(true).orden(16).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C17").nombre("Se comunica oralmente en inglés como lengua extranjera.").activo(true).orden(17).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C18").nombre("Lee diversos tipos de textos en inglés como lengua extranjera.").activo(true).orden(18).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C19").nombre("Escribe diversos tipos de textos en inglés como lengua extranjera.").activo(true).orden(19).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C20").nombre("Resuelve problemas de cantidad.").activo(true).orden(20).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C21").nombre("Resuelve problemas de regularidad, equivalencia y cambio.").activo(true).orden(21).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C22").nombre("Resuelve problemas de forma, movimiento y localización.").activo(true).orden(22).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C23").nombre("Resuelve problemas de gestión de datos e incertidumbre.").activo(true).orden(23).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C24").nombre("Indaga mediante métodos científicos para construir conocimientos.").activo(true).orden(24).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C25").nombre("Explica el mundo físico basándose en conocimientos sobre los seres vivos, materia y energía, biodiversidad, Tierra y Universo.").activo(true).orden(25).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C26").nombre("Diseña y construye soluciones tecnológicas para resolver problemas de su entorno.").activo(true).orden(26).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C27").nombre("Se desenvuelve en entornos virtuales generados por las TIC.").activo(true).orden(27).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C28").nombre("Gestiona su aprendizaje de manera autónoma.").activo(true).orden(28).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C29").nombre("Lee diversos tipos de textos escritos en castellano como segunda lengua.").activo(true).orden(29).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C30").nombre("Escribe diversos tipos de textos en castellano como segunda lengua.").activo(true).orden(30).build()
                );
                catalogoRepo.saveAll(competenciasPrimaria);
                log.info("{} competencias de primaria creadas", competenciasPrimaria.size());

                // Mapeos de Competencias por Curso en PRIMARIA
                List<CatalogoAcademico> competenciaCursosPrim = List.of(
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("PRIMARIA").codigo("INGLES").nombre("C17,C18,C19").activo(true).orden(1).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("PRIMARIA").codigo("PERSONAL_SOCIAL").nombre("C1,C2,C3,C4,C5").activo(true).orden(2).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("PRIMARIA").codigo("EDUCACION_RELIGIOSA").nombre("C6,C7").activo(true).orden(3).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("PRIMARIA").codigo("EDUCACION_FISICA").nombre("C8,C9,C10").activo(true).orden(4).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("PRIMARIA").codigo("COMUNICACION").nombre("C11,C12,C13").activo(true).orden(5).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("PRIMARIA").codigo("ARTE_CULTURA").nombre("C14,C15").activo(true).orden(6).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("PRIMARIA").codigo("CASTELLANO_SEGUNDA_LENGUA").nombre("C16,C29,C30").activo(true).orden(7).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("PRIMARIA").codigo("MATEMATICA").nombre("C20,C21,C22,C23").activo(true).orden(8).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("PRIMARIA").codigo("CIENCIA_TECNOLOGIA").nombre("C24,C25,C26").activo(true).orden(9).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("PRIMARIA").codigo("COMPETENCIAS_TRANSVERSALES").nombre("C27,C28").activo(true).orden(10).build()
                );
                catalogoRepo.saveAll(competenciaCursosPrim);
                log.info("{} mapeos curso-competencia de primaria creados", competenciaCursosPrim.size());

                // Mapeos de Docentes por Competencia en PRIMARIA (1ro a 6to)
                Map<String, List<String>> docenteMap = Map.ofEntries(
                        // Inglés C17, C18, C19 (Daniela: 10000001)
                        Map.entry("PRIMERO_PRIMARIA||INGLES||C17", List.of("10000001")),
                        Map.entry("PRIMERO_PRIMARIA||INGLES||C18", List.of("10000001")),
                        Map.entry("PRIMERO_PRIMARIA||INGLES||C19", List.of("10000001")),
                        Map.entry("SEGUNDO_PRIMARIA||INGLES||C17", List.of("10000001")),
                        Map.entry("SEGUNDO_PRIMARIA||INGLES||C18", List.of("10000001")),
                        Map.entry("SEGUNDO_PRIMARIA||INGLES||C19", List.of("10000001")),
                        Map.entry("TERCERO_PRIMARIA||INGLES||C17", List.of("10000001")),
                        Map.entry("TERCERO_PRIMARIA||INGLES||C18", List.of("10000001")),
                        Map.entry("TERCERO_PRIMARIA||INGLES||C19", List.of("10000001")),
                        Map.entry("CUARTO_PRIMARIA||INGLES||C17", List.of("10000001")),
                        Map.entry("CUARTO_PRIMARIA||INGLES||C18", List.of("10000001")),
                        Map.entry("CUARTO_PRIMARIA||INGLES||C19", List.of("10000001")),
                        Map.entry("QUINTO_PRIMARIA||INGLES||C17", List.of("10000001")),
                        Map.entry("QUINTO_PRIMARIA||INGLES||C18", List.of("10000001")),
                        Map.entry("QUINTO_PRIMARIA||INGLES||C19", List.of("10000001")),
                        Map.entry("SEXTO_PRIMARIA||INGLES||C17", List.of("10000001")),
                        Map.entry("SEXTO_PRIMARIA||INGLES||C18", List.of("10000001")),
                        Map.entry("SEXTO_PRIMARIA||INGLES||C19", List.of("10000001")),

                        // Personal Social C1-C3, C5 (1ro: Leslie: 10000002)
                        Map.entry("PRIMERO_PRIMARIA||PERSONAL_SOCIAL||C1", List.of("10000002")),
                        Map.entry("PRIMERO_PRIMARIA||PERSONAL_SOCIAL||C2", List.of("10000002")),
                        Map.entry("PRIMERO_PRIMARIA||PERSONAL_SOCIAL||C3", List.of("10000002")),
                        Map.entry("PRIMERO_PRIMARIA||PERSONAL_SOCIAL||C5", List.of("10000002")),

                        // Personal Social C1-C3 (2do: Mirian Diego: 10000003)
                        Map.entry("SEGUNDO_PRIMARIA||PERSONAL_SOCIAL||C1", List.of("10000003")),
                        Map.entry("SEGUNDO_PRIMARIA||PERSONAL_SOCIAL||C2", List.of("10000003")),
                        Map.entry("SEGUNDO_PRIMARIA||PERSONAL_SOCIAL||C3", List.of("10000003")),

                        // Personal Social C1-C5 (3ro, 4to, 5to: Karin: 10000004)
                        Map.entry("TERCERO_PRIMARIA||PERSONAL_SOCIAL||C1", List.of("10000006", "10000007")),
                        Map.entry("TERCERO_PRIMARIA||PERSONAL_SOCIAL||C2", List.of("10000004")),
                        Map.entry("TERCERO_PRIMARIA||PERSONAL_SOCIAL||C3", List.of("10000004")),
                        Map.entry("TERCERO_PRIMARIA||PERSONAL_SOCIAL||C4", List.of("10000006", "10000007")),
                        Map.entry("TERCERO_PRIMARIA||PERSONAL_SOCIAL||C5", List.of("10000004")),
                        Map.entry("CUARTO_PRIMARIA||PERSONAL_SOCIAL||C1", List.of("10000006", "10000007")),
                        Map.entry("CUARTO_PRIMARIA||PERSONAL_SOCIAL||C2", List.of("10000004")),
                        Map.entry("CUARTO_PRIMARIA||PERSONAL_SOCIAL||C3", List.of("10000004")),
                        Map.entry("CUARTO_PRIMARIA||PERSONAL_SOCIAL||C4", List.of("10000006", "10000007")),
                        Map.entry("CUARTO_PRIMARIA||PERSONAL_SOCIAL||C5", List.of("10000004")),
                        Map.entry("QUINTO_PRIMARIA||PERSONAL_SOCIAL||C1", List.of("10000006", "10000007")),
                        Map.entry("QUINTO_PRIMARIA||PERSONAL_SOCIAL||C2", List.of("10000004")),
                        Map.entry("QUINTO_PRIMARIA||PERSONAL_SOCIAL||C3", List.of("10000004")),
                        Map.entry("QUINTO_PRIMARIA||PERSONAL_SOCIAL||C4", List.of("10000006", "10000007")),
                        Map.entry("QUINTO_PRIMARIA||PERSONAL_SOCIAL||C5", List.of("10000004")),

                        // Personal Social C1-C3 (6to: Rosvita: 10000005)
                        Map.entry("SEXTO_PRIMARIA||PERSONAL_SOCIAL||C1", List.of("10000005")),
                        Map.entry("SEXTO_PRIMARIA||PERSONAL_SOCIAL||C2", List.of("10000005")),
                        Map.entry("SEXTO_PRIMARIA||PERSONAL_SOCIAL||C3", List.of("10000005")),
                        // Personal Social C4 (6to: Karin: 10000004)
                        Map.entry("SEXTO_PRIMARIA||PERSONAL_SOCIAL||C4", List.of("10000004")),

                        // Educación Religiosa C6-C7
                        Map.entry("PRIMERO_PRIMARIA||EDUCACION_RELIGIOSA||C6", List.of("10000002")),
                        Map.entry("PRIMERO_PRIMARIA||EDUCACION_RELIGIOSA||C7", List.of("10000002")),
                        Map.entry("SEGUNDO_PRIMARIA||EDUCACION_RELIGIOSA||C6", List.of("10000003")),
                        Map.entry("SEGUNDO_PRIMARIA||EDUCACION_RELIGIOSA||C7", List.of("10000003")),
                        Map.entry("TERCERO_PRIMARIA||EDUCACION_RELIGIOSA||C6", List.of("10000001")),
                        Map.entry("TERCERO_PRIMARIA||EDUCACION_RELIGIOSA||C7", List.of("10000001")),
                        Map.entry("CUARTO_PRIMARIA||EDUCACION_RELIGIOSA||C6", List.of("10000006", "10000007")),
                        Map.entry("CUARTO_PRIMARIA||EDUCACION_RELIGIOSA||C7", List.of("10000006", "10000007")),
                        Map.entry("QUINTO_PRIMARIA||EDUCACION_RELIGIOSA||C6", List.of("10000006", "10000007")),
                        Map.entry("QUINTO_PRIMARIA||EDUCACION_RELIGIOSA||C7", List.of("10000006", "10000007")),
                        Map.entry("SEXTO_PRIMARIA||EDUCACION_RELIGIOSA||C6", List.of("10000004")),
                        Map.entry("SEXTO_PRIMARIA||EDUCACION_RELIGIOSA||C7", List.of("10000004")),

                        // Educación Física C8-C10
                        Map.entry("PRIMERO_PRIMARIA||EDUCACION_FISICA||C8", List.of("10000002")),
                        Map.entry("PRIMERO_PRIMARIA||EDUCACION_FISICA||C9", List.of("10000002")),
                        Map.entry("PRIMERO_PRIMARIA||EDUCACION_FISICA||C10", List.of("10000002")),
                        Map.entry("SEGUNDO_PRIMARIA||EDUCACION_FISICA||C8", List.of("10000003")),
                        Map.entry("SEGUNDO_PRIMARIA||EDUCACION_FISICA||C9", List.of("10000003")),
                        Map.entry("SEGUNDO_PRIMARIA||EDUCACION_FISICA||C10", List.of("10000003")),
                        Map.entry("TERCERO_PRIMARIA||EDUCACION_FISICA||C8", List.of("10000008")),
                        Map.entry("TERCERO_PRIMARIA||EDUCACION_FISICA||C9", List.of("10000008")),
                        Map.entry("TERCERO_PRIMARIA||EDUCACION_FISICA||C10", List.of("10000008")),
                        Map.entry("CUARTO_PRIMARIA||EDUCACION_FISICA||C8", List.of("10000008")),
                        Map.entry("CUARTO_PRIMARIA||EDUCACION_FISICA||C9", List.of("10000008")),
                        Map.entry("CUARTO_PRIMARIA||EDUCACION_FISICA||C10", List.of("10000008")),
                        Map.entry("QUINTO_PRIMARIA||EDUCACION_FISICA||C8", List.of("10000008")),
                        Map.entry("QUINTO_PRIMARIA||EDUCACION_FISICA||C9", List.of("10000008")),
                        Map.entry("QUINTO_PRIMARIA||EDUCACION_FISICA||C10", List.of("10000008")),
                        Map.entry("SEXTO_PRIMARIA||EDUCACION_FISICA||C8", List.of("10000008")),
                        Map.entry("SEXTO_PRIMARIA||EDUCACION_FISICA||C9", List.of("10000008")),
                        Map.entry("SEXTO_PRIMARIA||EDUCACION_FISICA||C10", List.of("10000008")),

                        // Comunicación C11-C13
                        Map.entry("PRIMERO_PRIMARIA||COMUNICACION||C11", List.of("10000002")),
                        Map.entry("PRIMERO_PRIMARIA||COMUNICACION||C12", List.of("10000002")),
                        Map.entry("PRIMERO_PRIMARIA||COMUNICACION||C13", List.of("10000002")),
                        Map.entry("SEGUNDO_PRIMARIA||COMUNICACION||C11", List.of("10000003")),
                        Map.entry("SEGUNDO_PRIMARIA||COMUNICACION||C12", List.of("10000003")),
                        Map.entry("SEGUNDO_PRIMARIA||COMUNICACION||C13", List.of("10000003")),
                        Map.entry("TERCERO_PRIMARIA||COMUNICACION||C11", List.of("10000004")),
                        Map.entry("TERCERO_PRIMARIA||COMUNICACION||C12", List.of("10000004")),
                        Map.entry("TERCERO_PRIMARIA||COMUNICACION||C13", List.of("10000004")),
                        Map.entry("CUARTO_PRIMARIA||COMUNICACION||C11", List.of("10000004")),
                        Map.entry("CUARTO_PRIMARIA||COMUNICACION||C12", List.of("10000009")),
                        Map.entry("CUARTO_PRIMARIA||COMUNICACION||C13", List.of("10000004")),
                        Map.entry("QUINTO_PRIMARIA||COMUNICACION||C11", List.of("10000009")),
                        Map.entry("QUINTO_PRIMARIA||COMUNICACION||C12", List.of("10000009")),
                        Map.entry("QUINTO_PRIMARIA||COMUNICACION||C13", List.of("10000009")),
                        Map.entry("SEXTO_PRIMARIA||COMUNICACION||C11", List.of("10000009")),
                        Map.entry("SEXTO_PRIMARIA||COMUNICACION||C12", List.of("10000009")),
                        Map.entry("SEXTO_PRIMARIA||COMUNICACION||C13", List.of("10000009")),

                        // Arte y Cultura C14-C15
                        Map.entry("PRIMERO_PRIMARIA||ARTE_CULTURA||C14", List.of("10000002")),
                        Map.entry("PRIMERO_PRIMARIA||ARTE_CULTURA||C15", List.of("10000002")),
                        Map.entry("SEGUNDO_PRIMARIA||ARTE_CULTURA||C14", List.of("10000003")),
                        Map.entry("SEGUNDO_PRIMARIA||ARTE_CULTURA||C15", List.of("10000003")),
                        Map.entry("TERCERO_PRIMARIA||ARTE_CULTURA||C14", List.of("10000010")),
                        Map.entry("TERCERO_PRIMARIA||ARTE_CULTURA||C15", List.of("10000010")),
                        Map.entry("CUARTO_PRIMARIA||ARTE_CULTURA||C14", List.of("10000010")),
                        Map.entry("CUARTO_PRIMARIA||ARTE_CULTURA||C15", List.of("10000010")),
                        Map.entry("QUINTO_PRIMARIA||ARTE_CULTURA||C14", List.of("10000010")),
                        Map.entry("QUINTO_PRIMARIA||ARTE_CULTURA||C15", List.of("10000010")),
                        Map.entry("SEXTO_PRIMARIA||ARTE_CULTURA||C14", List.of("10000010")),
                        Map.entry("SEXTO_PRIMARIA||ARTE_CULTURA||C15", List.of("10000010")),

                        // Ciencia y Tecnología C24-C26
                        Map.entry("PRIMERO_PRIMARIA||CIENCIA_TECNOLOGIA||C24", List.of("10000002")),
                        Map.entry("PRIMERO_PRIMARIA||CIENCIA_TECNOLOGIA||C25", List.of("10000002")),
                        Map.entry("PRIMERO_PRIMARIA||CIENCIA_TECNOLOGIA||C26", List.of("10000002")),
                        Map.entry("SEGUNDO_PRIMARIA||CIENCIA_TECNOLOGIA||C24", List.of("10000003")),
                        Map.entry("SEGUNDO_PRIMARIA||CIENCIA_TECNOLOGIA||C25", List.of("10000003")),
                        Map.entry("SEGUNDO_PRIMARIA||CIENCIA_TECNOLOGIA||C26", List.of("10000003")),
                        Map.entry("TERCERO_PRIMARIA||CIENCIA_TECNOLOGIA||C24", List.of("10000004")),
                        Map.entry("TERCERO_PRIMARIA||CIENCIA_TECNOLOGIA||C25", List.of("10000004")),
                        Map.entry("TERCERO_PRIMARIA||CIENCIA_TECNOLOGIA||C26", List.of("10000004")),
                        Map.entry("CUARTO_PRIMARIA||CIENCIA_TECNOLOGIA||C24", List.of("10000011")),
                        Map.entry("CUARTO_PRIMARIA||CIENCIA_TECNOLOGIA||C25", List.of("10000011")),
                        Map.entry("CUARTO_PRIMARIA||CIENCIA_TECNOLOGIA||C26", List.of("10000011")),
                        Map.entry("QUINTO_PRIMARIA||CIENCIA_TECNOLOGIA||C24", List.of("10000011")),
                        Map.entry("QUINTO_PRIMARIA||CIENCIA_TECNOLOGIA||C25", List.of("10000011")),
                        Map.entry("QUINTO_PRIMARIA||CIENCIA_TECNOLOGIA||C26", List.of("10000011")),
                        Map.entry("SEXTO_PRIMARIA||CIENCIA_TECNOLOGIA||C24", List.of("10000011")),
                        Map.entry("SEXTO_PRIMARIA||CIENCIA_TECNOLOGIA||C25", List.of("10000011")),
                        Map.entry("SEXTO_PRIMARIA||CIENCIA_TECNOLOGIA||C26", List.of("10000011")),

                        // Matemática (primeras 4 competencias por grado)
                        Map.entry("PRIMERO_PRIMARIA||MATEMATICA||C20", List.of("10000002")),
                        Map.entry("PRIMERO_PRIMARIA||MATEMATICA||C21", List.of("10000002")),
                        Map.entry("PRIMERO_PRIMARIA||MATEMATICA||C22", List.of("10000002")),
                        Map.entry("PRIMERO_PRIMARIA||MATEMATICA||C23", List.of("10000002")),
                        Map.entry("SEGUNDO_PRIMARIA||MATEMATICA||C20", List.of("10000003")),
                        Map.entry("SEGUNDO_PRIMARIA||MATEMATICA||C21", List.of("10000003")),
                        Map.entry("SEGUNDO_PRIMARIA||MATEMATICA||C22", List.of("10000003")),
                        Map.entry("SEGUNDO_PRIMARIA||MATEMATICA||C23", List.of("10000003")),
                        Map.entry("TERCERO_PRIMARIA||MATEMATICA||C20", List.of("10000006", "10000007")),
                        Map.entry("TERCERO_PRIMARIA||MATEMATICA||C21", List.of("10000011")),
                        Map.entry("TERCERO_PRIMARIA||MATEMATICA||C22", List.of("10000004")),
                        Map.entry("TERCERO_PRIMARIA||MATEMATICA||C23", List.of("10000006", "10000007")),
                        Map.entry("CUARTO_PRIMARIA||MATEMATICA||C20", List.of("10000006", "10000007")),
                        Map.entry("CUARTO_PRIMARIA||MATEMATICA||C21", List.of("10000012")),
                        Map.entry("CUARTO_PRIMARIA||MATEMATICA||C22", List.of("10000012")),
                        Map.entry("CUARTO_PRIMARIA||MATEMATICA||C23", List.of("10000006", "10000007")),
                        Map.entry("QUINTO_PRIMARIA||MATEMATICA||C20", List.of("10000006", "10000007")),
                        Map.entry("QUINTO_PRIMARIA||MATEMATICA||C21", List.of("10000012")),
                        Map.entry("QUINTO_PRIMARIA||MATEMATICA||C22", List.of("10000012")),
                        Map.entry("QUINTO_PRIMARIA||MATEMATICA||C23", List.of("10000006", "10000007")),
                        Map.entry("SEXTO_PRIMARIA||MATEMATICA||C20", List.of("10000013")),
                        Map.entry("SEXTO_PRIMARIA||MATEMATICA||C21", List.of("10000012")),
                        Map.entry("SEXTO_PRIMARIA||MATEMATICA||C22", List.of("10000012")),
                        Map.entry("SEXTO_PRIMARIA||MATEMATICA||C23", List.of("10000013"))
                );

                int idx = 5000;
                List<CatalogoAcademico> docentesCompetenciasPrim = new ArrayList<>();
                for (Map.Entry<String, List<String>> entry : docenteMap.entrySet()) {
                    docentesCompetenciasPrim.add(CatalogoAcademico.builder()
                            .tipo("DOCENTE_COMPETENCIA")
                            .nivel("PRIMARIA")
                            .codigo(entry.getKey())
                            .nombre(serializeDocentes(entry.getValue()))
                            .activo(true)
                            .orden(idx++)
                            .build());
                }
                catalogoRepo.saveAll(docentesCompetenciasPrim);
                log.info("{} mapeos docente-competencia de primaria creados", docentesCompetenciasPrim.size());
            } else {
                log.info("Áreas curriculares, competencias y docentes de PRIMARIA ya existen y están mapeadas");
            }

            // Crear áreas curriculares, competencias y mapeos de SECUNDARIA
            boolean competenciasSecExisten = catalogoRepo.findAll().stream()
                    .anyMatch(c -> "CS1".equals(c.getCodigo()) && "SECUNDARIA".equals(c.getNivel()));
            boolean docentesCompetenciasSecExisten = catalogoRepo.findAll().stream()
                    .anyMatch(c -> "DOCENTE_COMPETENCIA".equals(c.getTipo()) && "SECUNDARIA".equals(c.getNivel()));
            
            if (!competenciasSecExisten || !docentesCompetenciasSecExisten) {
                log.info("Recreando áreas curriculares, competencias y docentes por competencia de SECUNDARIA...");
                List<CatalogoAcademico> aEliminarSec = catalogoRepo.findAll().stream()
                        .filter(c -> "SECUNDARIA".equals(c.getNivel()) && 
                                ("CURSO".equals(c.getTipo()) || 
                                 "COMPETENCIA".equals(c.getTipo()) || 
                                 "DOCENTE_COMPETENCIA".equals(c.getTipo()) || 
                                 "COMPETENCIA_CURSO".equals(c.getTipo())))
                        .toList();
                if (!aEliminarSec.isEmpty()) {
                    catalogoRepo.deleteAll(aEliminarSec);
                }
                
                // Cursos de SECUNDARIA
                List<CatalogoAcademico> cursosSecundaria = List.of(
                        CatalogoAcademico.builder().tipo("CURSO").nivel("SECUNDARIA").codigo("DPCC").nombre("Desarrollo Personal, Ciudadanía y Cívica").activo(true).orden(1).build(),
                        CatalogoAcademico.builder().tipo("CURSO").nivel("SECUNDARIA").codigo("CIENCIAS_SOCIALES").nombre("Ciencias Sociales").activo(true).orden(2).build(),
                        CatalogoAcademico.builder().tipo("CURSO").nivel("SECUNDARIA").codigo("EDUCACION_RELIGIOSA").nombre("Educación Religiosa").activo(true).orden(3).build(),
                        CatalogoAcademico.builder().tipo("CURSO").nivel("SECUNDARIA").codigo("EDUCACION_TRABAJO").nombre("Educación para el Trabajo").activo(true).orden(4).build(),
                        CatalogoAcademico.builder().tipo("CURSO").nivel("SECUNDARIA").codigo("EDUCACION_FISICA").nombre("Educación Física").activo(true).orden(5).build(),
                        CatalogoAcademico.builder().tipo("CURSO").nivel("SECUNDARIA").codigo("COMUNICACION").nombre("Comunicación").activo(true).orden(6).build(),
                        CatalogoAcademico.builder().tipo("CURSO").nivel("SECUNDARIA").codigo("ARTE_CULTURA").nombre("Arte y Cultura").activo(true).orden(7).build(),
                        CatalogoAcademico.builder().tipo("CURSO").nivel("SECUNDARIA").codigo("CASTELLANO_SEGUNDA_LENGUA").nombre("Castellano como Segunda Lengua").activo(true).orden(8).build(),
                        CatalogoAcademico.builder().tipo("CURSO").nivel("SECUNDARIA").codigo("INGLES").nombre("Inglés").activo(true).orden(9).build(),
                        CatalogoAcademico.builder().tipo("CURSO").nivel("SECUNDARIA").codigo("MATEMATICA").nombre("Matemática").activo(true).orden(10).build(),
                        CatalogoAcademico.builder().tipo("CURSO").nivel("SECUNDARIA").codigo("CIENCIA_TECNOLOGIA").nombre("Ciencia y Tecnología").activo(true).orden(11).build()
                );
                catalogoRepo.saveAll(cursosSecundaria);
                log.info("{} cursos de secundaria creados", cursosSecundaria.size());
                
                // Competencias de SECUNDARIA
                List<CatalogoAcademico> competenciasSecundaria = List.of(
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS1").nombre("Construye su identidad.").activo(true).orden(1).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS2").nombre("Convive y participa democráticamente en la búsqueda del bien común.").activo(true).orden(2).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS3").nombre("Construye interpretaciones históricas.").activo(true).orden(3).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS4").nombre("Gestiona responsablemente el espacio y el ambiente.").activo(true).orden(4).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS5").nombre("Gestiona responsablemente los recursos económicos.").activo(true).orden(5).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS6").nombre("Construye su identidad como persona humana, amada por Dios, digna, libre y trascendente, comprendiendo la doctrina de su propia religión, abierto al diálogo con las que le son cercanas.").activo(true).orden(6).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS7").nombre("Asume la experiencia del encuentro personal y comunitario con Dios en su proyecto de vida en coherencia con su creencia religiosa.").activo(true).orden(7).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS8").nombre("Gestiona proyectos de emprendimiento económico o social.").activo(true).orden(8).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS9").nombre("Se desenvuelve de manera autónoma a través de su motricidad.").activo(true).orden(9).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS10").nombre("Asume una vida saludable.").activo(true).orden(10).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS11").nombre("Interactúa a través de sus habilidades sociomotrices.").activo(true).orden(11).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS12").nombre("Se comunica oralmente en su lengua materna.").activo(true).orden(12).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS13").nombre("Lee diversos tipos de textos escritos.").activo(true).orden(13).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS14").nombre("Escribe diversos tipos de textos.").activo(true).orden(14).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS15").nombre("Aprecia de manera crítica manifestaciones artístico-culturales.").activo(true).orden(15).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS16").nombre("Crea proyectos desde los lenguajes artísticos.").activo(true).orden(16).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS17").nombre("Se comunica oralmente en lengua materna.").activo(true).orden(17).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS18").nombre("Lee diversos tipos de textos escritos.").activo(true).orden(18).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS19").nombre("Escribe diversos tipos de textos.").activo(true).orden(19).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS20").nombre("Se comunica oralmente en inglés como lengua extranjera.").activo(true).orden(20).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS21").nombre("Lee diversos tipos de textos en inglés como lengua extranjera.").activo(true).orden(21).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS22").nombre("Escribe diversos tipos de textos en inglés como lengua extranjera.").activo(true).orden(22).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS23").nombre("Resuelve problemas de cantidad.").activo(true).orden(23).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS24").nombre("Resuelve problemas de regularidad, equivalencia y cambio.").activo(true).orden(24).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS25").nombre("Resuelve problemas de movimiento, forma y localización.").activo(true).orden(25).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS26").nombre("Resuelve problemas de gestión de datos e incertidumbre.").activo(true).orden(26).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS27").nombre("Indaga mediante métodos científicos para construir sus conocimientos.").activo(true).orden(27).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS28").nombre("Explica el mundo físico basándose en conocimientos sobre los seres vivos, materia y energía, biodiversidad, Tierra y universo.").activo(true).orden(28).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("SECUNDARIA").codigo("CS29").nombre("Diseña y construye soluciones tecnológicas para resolver problemas de su entorno.").activo(true).orden(29).build()
                );
                catalogoRepo.saveAll(competenciasSecundaria);
                log.info("{} competencias de secundaria creadas", competenciasSecundaria.size());
                
                // Mapeos de Competencias por Curso en SECUNDARIA
                List<CatalogoAcademico> competenciaCursosSec = List.of(
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("SECUNDARIA").codigo("DPCC").nombre("CS1,CS2").activo(true).orden(1).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("SECUNDARIA").codigo("CIENCIAS_SOCIALES").nombre("CS3,CS4,CS5").activo(true).orden(2).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("SECUNDARIA").codigo("EDUCACION_RELIGIOSA").nombre("CS6,CS7").activo(true).orden(3).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("SECUNDARIA").codigo("EDUCACION_TRABAJO").nombre("CS8").activo(true).orden(4).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("SECUNDARIA").codigo("EDUCACION_FISICA").nombre("CS9,CS10,CS11").activo(true).orden(5).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("SECUNDARIA").codigo("COMUNICACION").nombre("CS12,CS13,CS14").activo(true).orden(6).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("SECUNDARIA").codigo("ARTE_CULTURA").nombre("CS15,CS16").activo(true).orden(7).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("SECUNDARIA").codigo("CASTELLANO_SEGUNDA_LENGUA").nombre("CS17,CS18,CS19").activo(true).orden(8).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("SECUNDARIA").codigo("INGLES").nombre("CS20,CS21,CS22").activo(true).orden(9).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("SECUNDARIA").codigo("MATEMATICA").nombre("CS23,CS24,CS25,CS26").activo(true).orden(10).build(),
                        CatalogoAcademico.builder().tipo("COMPETENCIA_CURSO").nivel("SECUNDARIA").codigo("CIENCIA_TECNOLOGIA").nombre("CS27,CS28,CS29").activo(true).orden(11).build()
                );
                catalogoRepo.saveAll(competenciaCursosSec);
                log.info("{} mapeos curso-competencia de secundaria creados", competenciaCursosSec.size());

                // Mapeos de Docentes por Competencia en SECUNDARIA (1ro a 5to)
                Map<String, List<String>> docenteMapSec = Map.ofEntries(
                        // DPCC CS1: Adaluz Paye (20000001) para 1ro, 2do, 3ro; Rosvita Gómez (20000002) para 4to, 5to
                        Map.entry("PRIMERO_SECUNDARIA||DPCC||CS1", List.of("20000001")),
                        Map.entry("SEGUNDO_SECUNDARIA||DPCC||CS1", List.of("20000001")),
                        Map.entry("TERCERO_SECUNDARIA||DPCC||CS1", List.of("20000001")),
                        Map.entry("CUARTO_SECUNDARIA||DPCC||CS1", List.of("20000002")),
                        Map.entry("QUINTO_SECUNDARIA||DPCC||CS1", List.of("20000002")),

                        // DPCC CS2: Rosvita Gómez (20000002) para 1ro a 5to
                        Map.entry("PRIMERO_SECUNDARIA||DPCC||CS2", List.of("20000002")),
                        Map.entry("SEGUNDO_SECUNDARIA||DPCC||CS2", List.of("20000002")),
                        Map.entry("TERCERO_SECUNDARIA||DPCC||CS2", List.of("20000002")),
                        Map.entry("CUARTO_SECUNDARIA||DPCC||CS2", List.of("20000002")),
                        Map.entry("QUINTO_SECUNDARIA||DPCC||CS2", List.of("20000002")),

                        // Ciencias Sociales CS3, CS4, CS5: Rosvita Gómez (20000002) para 1ro a 5to
                        Map.entry("PRIMERO_SECUNDARIA||CIENCIAS_SOCIALES||CS3", List.of("20000002")),
                        Map.entry("PRIMERO_SECUNDARIA||CIENCIAS_SOCIALES||CS4", List.of("20000002")),
                        Map.entry("PRIMERO_SECUNDARIA||CIENCIAS_SOCIALES||CS5", List.of("20000002")),
                        Map.entry("SEGUNDO_SECUNDARIA||CIENCIAS_SOCIALES||CS3", List.of("20000002")),
                        Map.entry("SEGUNDO_SECUNDARIA||CIENCIAS_SOCIALES||CS4", List.of("20000002")),
                        Map.entry("SEGUNDO_SECUNDARIA||CIENCIAS_SOCIALES||CS5", List.of("20000002")),
                        Map.entry("TERCERO_SECUNDARIA||CIENCIAS_SOCIALES||CS3", List.of("20000002")),
                        Map.entry("TERCERO_SECUNDARIA||CIENCIAS_SOCIALES||CS4", List.of("20000002")),
                        Map.entry("TERCERO_SECUNDARIA||CIENCIAS_SOCIALES||CS5", List.of("20000002")),
                        Map.entry("CUARTO_SECUNDARIA||CIENCIAS_SOCIALES||CS3", List.of("20000002")),
                        Map.entry("CUARTO_SECUNDARIA||CIENCIAS_SOCIALES||CS4", List.of("20000002")),
                        Map.entry("CUARTO_SECUNDARIA||CIENCIAS_SOCIALES||CS5", List.of("20000002")),
                        Map.entry("QUINTO_SECUNDARIA||CIENCIAS_SOCIALES||CS3", List.of("20000002")),
                        Map.entry("QUINTO_SECUNDARIA||CIENCIAS_SOCIALES||CS4", List.of("20000002")),
                        Map.entry("QUINTO_SECUNDARIA||CIENCIAS_SOCIALES||CS5", List.of("20000002")),

                        // Educación Religiosa CS6, CS7
                        Map.entry("PRIMERO_SECUNDARIA||EDUCACION_RELIGIOSA||CS6", List.of("20000001")),
                        Map.entry("PRIMERO_SECUNDARIA||EDUCACION_RELIGIOSA||CS7", List.of("20000001")),
                        Map.entry("SEGUNDO_SECUNDARIA||EDUCACION_RELIGIOSA||CS6", List.of("20000003")),
                        Map.entry("SEGUNDO_SECUNDARIA||EDUCACION_RELIGIOSA||CS7", List.of("20000003")),
                        Map.entry("TERCERO_SECUNDARIA||EDUCACION_RELIGIOSA||CS6", List.of("20000004")),
                        Map.entry("TERCERO_SECUNDARIA||EDUCACION_RELIGIOSA||CS7", List.of("20000004")),
                        Map.entry("CUARTO_SECUNDARIA||EDUCACION_RELIGIOSA||CS6", List.of("20000002")),
                        Map.entry("CUARTO_SECUNDARIA||EDUCACION_RELIGIOSA||CS7", List.of("20000002")),
                        Map.entry("QUINTO_SECUNDARIA||EDUCACION_RELIGIOSA||CS6", List.of("20000002")),
                        Map.entry("QUINTO_SECUNDARIA||EDUCACION_RELIGIOSA||CS7", List.of("20000002")),

                        // Educación para el Trabajo CS8: Adaluz Paye (20000001) para 1ro a 5to
                        Map.entry("PRIMERO_SECUNDARIA||EDUCACION_TRABAJO||CS8", List.of("20000005")),
                        Map.entry("SEGUNDO_SECUNDARIA||EDUCACION_TRABAJO||CS8", List.of("20000005")),
                        Map.entry("TERCERO_SECUNDARIA||EDUCACION_TRABAJO||CS8", List.of("20000005")),
                        Map.entry("CUARTO_SECUNDARIA||EDUCACION_TRABAJO||CS8", List.of("20000005")),
                        Map.entry("QUINTO_SECUNDARIA||EDUCACION_TRABAJO||CS8", List.of("20000005")),

                        // Comunicación CS12, CS13, CS14: Miriam Marcelo (20000003) para 1ro a 5to
                        Map.entry("PRIMERO_SECUNDARIA||COMUNICACION||CS12", List.of("20000003")),
                        Map.entry("PRIMERO_SECUNDARIA||COMUNICACION||CS13", List.of("20000003")),
                        Map.entry("PRIMERO_SECUNDARIA||COMUNICACION||CS14", List.of("20000003")),
                        Map.entry("SEGUNDO_SECUNDARIA||COMUNICACION||CS12", List.of("20000003")),
                        Map.entry("SEGUNDO_SECUNDARIA||COMUNICACION||CS13", List.of("20000003")),
                        Map.entry("SEGUNDO_SECUNDARIA||COMUNICACION||CS14", List.of("20000003")),
                        Map.entry("TERCERO_SECUNDARIA||COMUNICACION||CS12", List.of("20000003")),
                        Map.entry("TERCERO_SECUNDARIA||COMUNICACION||CS13", List.of("20000003")),
                        Map.entry("TERCERO_SECUNDARIA||COMUNICACION||CS14", List.of("20000003")),
                        Map.entry("CUARTO_SECUNDARIA||COMUNICACION||CS12", List.of("20000003")),
                        Map.entry("CUARTO_SECUNDARIA||COMUNICACION||CS13", List.of("20000003")),
                        Map.entry("CUARTO_SECUNDARIA||COMUNICACION||CS14", List.of("20000003")),
                        Map.entry("QUINTO_SECUNDARIA||COMUNICACION||CS12", List.of("20000003")),
                        Map.entry("QUINTO_SECUNDARIA||COMUNICACION||CS13", List.of("20000003")),
                        Map.entry("QUINTO_SECUNDARIA||COMUNICACION||CS14", List.of("20000003")),

                        // Arte y Cultura CS15, CS16: Adaluz Paye (20000001) para 1ro a 5to
                        Map.entry("PRIMERO_SECUNDARIA||ARTE_CULTURA||CS15", List.of("20000001")),
                        Map.entry("PRIMERO_SECUNDARIA||ARTE_CULTURA||CS16", List.of("20000001")),
                        Map.entry("SEGUNDO_SECUNDARIA||ARTE_CULTURA||CS15", List.of("20000001")),
                        Map.entry("SEGUNDO_SECUNDARIA||ARTE_CULTURA||CS16", List.of("20000001")),
                        Map.entry("TERCERO_SECUNDARIA||ARTE_CULTURA||CS15", List.of("20000001")),
                        Map.entry("TERCERO_SECUNDARIA||ARTE_CULTURA||CS16", List.of("20000001")),
                        Map.entry("CUARTO_SECUNDARIA||ARTE_CULTURA||CS15", List.of("20000001")),
                        Map.entry("CUARTO_SECUNDARIA||ARTE_CULTURA||CS16", List.of("20000001")),
                        Map.entry("QUINTO_SECUNDARIA||ARTE_CULTURA||CS15", List.of("20000001")),
                        Map.entry("QUINTO_SECUNDARIA||ARTE_CULTURA||CS16", List.of("20000001")),

                        // Inglés CS20, CS21, CS22: Daniela Ydrogo (20000005) para 1ro a 5to
                        Map.entry("PRIMERO_SECUNDARIA||INGLES||CS20", List.of("20000005")),
                        Map.entry("PRIMERO_SECUNDARIA||INGLES||CS21", List.of("20000005")),
                        Map.entry("PRIMERO_SECUNDARIA||INGLES||CS22", List.of("20000005")),
                        Map.entry("SEGUNDO_SECUNDARIA||INGLES||CS20", List.of("20000005")),
                        Map.entry("SEGUNDO_SECUNDARIA||INGLES||CS21", List.of("20000005")),
                        Map.entry("SEGUNDO_SECUNDARIA||INGLES||CS22", List.of("20000005")),
                        Map.entry("TERCERO_SECUNDARIA||INGLES||CS20", List.of("20000005")),
                        Map.entry("TERCERO_SECUNDARIA||INGLES||CS21", List.of("20000005")),
                        Map.entry("TERCERO_SECUNDARIA||INGLES||CS22", List.of("20000005")),
                        Map.entry("CUARTO_SECUNDARIA||INGLES||CS20", List.of("20000005")),
                        Map.entry("CUARTO_SECUNDARIA||INGLES||CS21", List.of("20000005")),
                        Map.entry("CUARTO_SECUNDARIA||INGLES||CS22", List.of("20000005")),
                        Map.entry("QUINTO_SECUNDARIA||INGLES||CS20", List.of("20000005")),
                        Map.entry("QUINTO_SECUNDARIA||INGLES||CS21", List.of("20000005")),
                        Map.entry("QUINTO_SECUNDARIA||INGLES||CS22", List.of("20000005")),

                        // Matemática
                        // CS23 (Cantidad): 1ro, 2do -> Omar Bruno (20000006); 3ro, 4to, 5to -> Eladio Magariño (20000008)
                        Map.entry("PRIMERO_SECUNDARIA||MATEMATICA||CS23", List.of("20000006")),
                        Map.entry("SEGUNDO_SECUNDARIA||MATEMATICA||CS23", List.of("20000006")),
                        Map.entry("TERCERO_SECUNDARIA||MATEMATICA||CS23", List.of("20000008")),
                        Map.entry("CUARTO_SECUNDARIA||MATEMATICA||CS23", List.of("20000008")),
                        Map.entry("QUINTO_SECUNDARIA||MATEMATICA||CS23", List.of("20000008")),

                        // CS24 (Regularidad): 1ro -> Christian Magariño (20000007); 2do a 5to -> Eladio Magariño (20000008)
                        Map.entry("PRIMERO_SECUNDARIA||MATEMATICA||CS24", List.of("20000007")),
                        Map.entry("SEGUNDO_SECUNDARIA||MATEMATICA||CS24", List.of("20000008")),
                        Map.entry("TERCERO_SECUNDARIA||MATEMATICA||CS24", List.of("20000008")),
                        Map.entry("CUARTO_SECUNDARIA||MATEMATICA||CS24", List.of("20000008")),
                        Map.entry("QUINTO_SECUNDARIA||MATEMATICA||CS24", List.of("20000008")),

                        // CS25 (Forma): 1ro, 2do -> Christian Magariño (20000007); 3ro, 4to, 5to -> Eladio Magariño (20000008)
                        Map.entry("PRIMERO_SECUNDARIA||MATEMATICA||CS25", List.of("20000007")),
                        Map.entry("SEGUNDO_SECUNDARIA||MATEMATICA||CS25", List.of("20000007")),
                        Map.entry("TERCERO_SECUNDARIA||MATEMATICA||CS25", List.of("20000008")),
                        Map.entry("CUARTO_SECUNDARIA||MATEMATICA||CS25", List.of("20000008")),
                        Map.entry("QUINTO_SECUNDARIA||MATEMATICA||CS25", List.of("20000008")),

                        // CS26 (Gestión de datos): 1ro, 2do -> Omar Bruno (20000006); 3ro, 4to, 5to -> Jhonatan Carhuancho (20000009)
                        Map.entry("PRIMERO_SECUNDARIA||MATEMATICA||CS26", List.of("20000006")),
                        Map.entry("SEGUNDO_SECUNDARIA||MATEMATICA||CS26", List.of("20000006")),
                        Map.entry("TERCERO_SECUNDARIA||MATEMATICA||CS26", List.of("20000009")),
                        Map.entry("CUARTO_SECUNDARIA||MATEMATICA||CS26", List.of("20000009")),
                        Map.entry("QUINTO_SECUNDARIA||MATEMATICA||CS26", List.of("20000009")),

                        // Ciencia y Tecnología
                        // CS27 (Indaga): 1ro a 5to -> Fernando Jacinto (20000010)
                        Map.entry("PRIMERO_SECUNDARIA||CIENCIA_TECNOLOGIA||CS27", List.of("20000010")),
                        Map.entry("SEGUNDO_SECUNDARIA||CIENCIA_TECNOLOGIA||CS27", List.of("20000010")),
                        Map.entry("TERCERO_SECUNDARIA||CIENCIA_TECNOLOGIA||CS27", List.of("20000010")),
                        Map.entry("CUARTO_SECUNDARIA||CIENCIA_TECNOLOGIA||CS27", List.of("20000010")),
                        Map.entry("QUINTO_SECUNDARIA||CIENCIA_TECNOLOGIA||CS27", List.of("20000010")),

                        // CS28 (Mundo físico): 1ro, 2do -> Lourdes Bonilla (20000004); 3ro, 4to, 5to -> Zenon Meza (20000011)
                        Map.entry("PRIMERO_SECUNDARIA||CIENCIA_TECNOLOGIA||CS28", List.of("20000004")),
                        Map.entry("SEGUNDO_SECUNDARIA||CIENCIA_TECNOLOGIA||CS28", List.of("20000004")),
                        Map.entry("TERCERO_SECUNDARIA||CIENCIA_TECNOLOGIA||CS28", List.of("20000011")),
                        Map.entry("CUARTO_SECUNDARIA||CIENCIA_TECNOLOGIA||CS28", List.of("20000011")),
                        Map.entry("QUINTO_SECUNDARIA||CIENCIA_TECNOLOGIA||CS28", List.of("20000011")),

                        // CS29 (Diseña): 1ro -> Omar Bruno (20000006) y Lourdes Bonilla (20000004); 2do -> Omar Bruno (20000006); 3ro, 4to, 5to -> César Veliz (20000012)
                        Map.entry("PRIMERO_SECUNDARIA||CIENCIA_TECNOLOGIA||CS29", List.of("20000006", "20000004")),
                        Map.entry("SEGUNDO_SECUNDARIA||CIENCIA_TECNOLOGIA||CS29", List.of("20000006")),
                        Map.entry("TERCERO_SECUNDARIA||CIENCIA_TECNOLOGIA||CS29", List.of("20000012")),
                        Map.entry("CUARTO_SECUNDARIA||CIENCIA_TECNOLOGIA||CS29", List.of("20000012")),
                        Map.entry("QUINTO_SECUNDARIA||CIENCIA_TECNOLOGIA||CS29", List.of("20000012"))
                );

                int idxSec = 6000;
                List<CatalogoAcademico> docentesCompetenciasSec = new ArrayList<>();
                for (Map.Entry<String, List<String>> entry : docenteMapSec.entrySet()) {
                    docentesCompetenciasSec.add(CatalogoAcademico.builder()
                            .tipo("DOCENTE_COMPETENCIA")
                            .nivel("SECUNDARIA")
                            .codigo(entry.getKey())
                            .nombre(serializeDocentes(entry.getValue()))
                            .activo(true)
                            .orden(idxSec++)
                            .build());
                }
                catalogoRepo.saveAll(docentesCompetenciasSec);
                log.info("{} mapeos docente-competencia de secundaria creados", docentesCompetenciasSec.size());
            } else {
                log.info("Áreas curriculares, competencias y docentes de SECUNDARIA ya existen y están mapeadas");
            }

            // Crear grados de SECUNDARIA
            boolean gradosSecExisten = catalogoRepo.findAll().stream()
                    .anyMatch(c -> "PRIMERO_SECUNDARIA".equals(c.getCodigo()) && "SECUNDARIA".equals(c.getNivel()));
            if (!gradosSecExisten) {
                log.info("Iniciando creación de grados de SECUNDARIA...");
                List<CatalogoAcademico> gradosSecundaria = List.of(
                        CatalogoAcademico.builder().tipo("GRADO").nivel("SECUNDARIA").codigo("PRIMERO_SECUNDARIA").nombre("Primer Grado Sec").activo(true).orden(1).build(),
                        CatalogoAcademico.builder().tipo("GRADO").nivel("SECUNDARIA").codigo("SEGUNDO_SECUNDARIA").nombre("Segundo Grado Sec").activo(true).orden(2).build(),
                        CatalogoAcademico.builder().tipo("GRADO").nivel("SECUNDARIA").codigo("TERCERO_SECUNDARIA").nombre("Tercer Grado Sec").activo(true).orden(3).build(),
                        CatalogoAcademico.builder().tipo("GRADO").nivel("SECUNDARIA").codigo("CUARTO_SECUNDARIA").nombre("Cuarto Grado Sec").activo(true).orden(4).build(),
                        CatalogoAcademico.builder().tipo("GRADO").nivel("SECUNDARIA").codigo("QUINTO_SECUNDARIA").nombre("Quinto Grado Sec").activo(true).orden(5).build()
                );
                catalogoRepo.saveAll(gradosSecundaria);
                log.info("{} grados de secundaria creados", gradosSecundaria.size());
            }

            // Crear secciones de SECUNDARIA
            boolean seccionesSecExisten = catalogoRepo.findAll().stream()
                    .anyMatch(c -> "A".equals(c.getCodigo()) && "SECCION".equals(c.getTipo()) && "SECUNDARIA".equals(c.getNivel()));
            if (!seccionesSecExisten) {
                log.info("Iniciando creación de secciones de SECUNDARIA...");
                List<CatalogoAcademico> seccionesSecundaria = List.of(
                        CatalogoAcademico.builder().tipo("SECCION").nivel("SECUNDARIA").codigo("A").nombre("Sección A").activo(true).orden(1).build(),
                        CatalogoAcademico.builder().tipo("SECCION").nivel("SECUNDARIA").codigo("B").nombre("Sección B").activo(true).orden(2).build(),
                        CatalogoAcademico.builder().tipo("SECCION").nivel("SECUNDARIA").codigo("C").nombre("Sección C").activo(true).orden(3).build()
                );
                catalogoRepo.saveAll(seccionesSecundaria);
                log.info("{} secciones de secundaria creadas", seccionesSecundaria.size());
            }

            // Crear grados de PRIMARIA
            boolean gradosExisten = catalogoRepo.findAll().stream()
                    .anyMatch(c -> "PRIMERO_PRIMARIA".equals(c.getCodigo()) && "PRIMARIA".equals(c.getNivel()));
            
            if (!gradosExisten) {
                log.info("Iniciando creación de grados de PRIMARIA...");
                
                List<CatalogoAcademico> gradosPrimaria = List.of(
                        CatalogoAcademico.builder().tipo("GRADO").nivel("PRIMARIA").codigo("PRIMERO_PRIMARIA").nombre("Primer Grado").activo(true).orden(1).build(),
                        CatalogoAcademico.builder().tipo("GRADO").nivel("PRIMARIA").codigo("SEGUNDO_PRIMARIA").nombre("Segundo Grado").activo(true).orden(2).build(),
                        CatalogoAcademico.builder().tipo("GRADO").nivel("PRIMARIA").codigo("TERCERO_PRIMARIA").nombre("Tercer Grado").activo(true).orden(3).build(),
                        CatalogoAcademico.builder().tipo("GRADO").nivel("PRIMARIA").codigo("CUARTO_PRIMARIA").nombre("Cuarto Grado").activo(true).orden(4).build(),
                        CatalogoAcademico.builder().tipo("GRADO").nivel("PRIMARIA").codigo("QUINTO_PRIMARIA").nombre("Quinto Grado").activo(true).orden(5).build(),
                        CatalogoAcademico.builder().tipo("GRADO").nivel("PRIMARIA").codigo("SEXTO_PRIMARIA").nombre("Sexto Grado").activo(true).orden(6).build()
                );
                catalogoRepo.saveAll(gradosPrimaria);
                log.info("{} grados de primaria creados", gradosPrimaria.size());
            } else {
                log.info("Grados de PRIMARIA ya existen");
            }

            // Crear secciones de PRIMARIA
            boolean seccionesExisten = catalogoRepo.findAll().stream()
                    .anyMatch(c -> "A".equals(c.getCodigo()) && "SECCION".equals(c.getTipo()) && "PRIMARIA".equals(c.getNivel()));
            
            if (!seccionesExisten) {
                log.info("Iniciando creación de secciones de PRIMARIA...");
                
                List<CatalogoAcademico> seccionesPrimaria = List.of(
                        CatalogoAcademico.builder().tipo("SECCION").nivel("PRIMARIA").codigo("A").nombre("Sección A").activo(true).orden(1).build(),
                        CatalogoAcademico.builder().tipo("SECCION").nivel("PRIMARIA").codigo("B").nombre("Sección B").activo(true).orden(2).build(),
                        CatalogoAcademico.builder().tipo("SECCION").nivel("PRIMARIA").codigo("C").nombre("Sección C").activo(true).orden(3).build()
                );
                catalogoRepo.saveAll(seccionesPrimaria);
                log.info("{} secciones de primaria creadas", seccionesPrimaria.size());
            } else {
                log.info("Secciones de PRIMARIA ya existen");
            }

            // Crear niveles académicos por defecto
            boolean nivelesExisten = catalogoRepo.findAll().stream()
                    .anyMatch(c -> "NIVEL_ACADEMICO".equals(c.getTipo()));
            
            if (!nivelesExisten) {
                log.info("Iniciando creación de niveles académicos...");
                List<CatalogoAcademico> niveles = List.of(
                        CatalogoAcademico.builder().tipo("NIVEL_ACADEMICO").nivel("GLOBAL").codigo("1RO_PRIM").nombre("1ro prim").activo(true).orden(1).build(),
                        CatalogoAcademico.builder().tipo("NIVEL_ACADEMICO").nivel("GLOBAL").codigo("2DO_PRIM").nombre("2do prim").activo(true).orden(2).build(),
                        CatalogoAcademico.builder().tipo("NIVEL_ACADEMICO").nivel("GLOBAL").codigo("3RO_PRIM").nombre("3ro prim").activo(true).orden(3).build(),
                        CatalogoAcademico.builder().tipo("NIVEL_ACADEMICO").nivel("GLOBAL").codigo("4TO_PRIM").nombre("4to prim").activo(true).orden(4).build(),
                        CatalogoAcademico.builder().tipo("NIVEL_ACADEMICO").nivel("GLOBAL").codigo("PREFORMATIVO").nombre("preformativo").activo(true).orden(5).build(),
                        CatalogoAcademico.builder().tipo("NIVEL_ACADEMICO").nivel("GLOBAL").codigo("CICLADO").nombre("ciclado").activo(true).orden(6).build(),
                        CatalogoAcademico.builder().tipo("NIVEL_ACADEMICO").nivel("GLOBAL").codigo("ANUAL").nombre("anual").activo(true).orden(7).build(),
                        CatalogoAcademico.builder().tipo("NIVEL_ACADEMICO").nivel("GLOBAL").codigo("LETRAS_CIENCIAS").nombre("Letras/Ciencias").activo(true).orden(8).build()
                );
                catalogoRepo.saveAll(niveles);
                log.info("{} niveles académicos creados por defecto", niveles.size());
            } else {
                log.info("Niveles académicos ya existen");
            }

            // Crear asignaciones automáticas basadas en los alumnos existentes y docentes
            if (asignacionRepo.count() == 0) {
                List<AsignacionAcademica> asignacionesDefault = new ArrayList<>();
                
                // Obtener todos los docentes y alumnos activos (USAR LOS ALUMNOS IMPORTADOS)
                List<UsuarioAcademico> docentes = usuarioAcademicoRepo.findByRolAndActivoTrue(RolUsuario.DOCENTE);
                List<UsuarioAcademico> alumnos = usuarioAcademicoRepo.findByRolAndActivoTrue(RolUsuario.ALUMNO);
                
                log.info("Creando asignaciones para {} alumnos existentes y {} docentes", alumnos.size(), docentes.size());
                
                // Por cada alumno existente, asignarle todos los docentes que tengan su grado
                for (UsuarioAcademico alumno : alumnos) {
                    for (UsuarioAcademico docente : docentes) {
                        // Solo asignar si son del mismo nivel educativo
                        if (alumno.getNivelEducativo() != null && 
                            docente.getNivelEducativo() != null &&
                            alumno.getNivelEducativo().equals(docente.getNivelEducativo())) {
                            
                            // Crear asignaciones con materias básicas
                            List<com.monserrat.entity.CursoAcademico> materias = new ArrayList<>();
                            if (alumno.getNivelEducativo().equals(com.monserrat.entity.NivelEducativo.PRIMARIA)) {
                                materias = List.of(
                                    com.monserrat.entity.CursoAcademico.PERSONAL_SOCIAL,
                                    com.monserrat.entity.CursoAcademico.MATEMATICA,
                                    com.monserrat.entity.CursoAcademico.COMUNICACION
                                );
                            } else {
                                materias = List.of(
                                    com.monserrat.entity.CursoAcademico.MATEMATICA,
                                    com.monserrat.entity.CursoAcademico.COMUNICACION,
                                    com.monserrat.entity.CursoAcademico.HISTORIA
                                );
                            }
                            
                            for (com.monserrat.entity.CursoAcademico materia : materias) {
                                boolean yaExiste = asignacionRepo.existsByDocente_DniAndAlumno_DniAndCursoAndGradoAndSeccionAndActivoTrue(
                                    docente.getDni(), alumno.getDni(), materia, alumno.getGrado(), alumno.getSeccion());
                                if (!yaExiste) {
                                    AsignacionAcademica asignacion = AsignacionAcademica.builder()
                                            .docente(docente)
                                            .alumno(alumno)
                                            .curso(materia)
                                            .nivelEducativo(alumno.getNivelEducativo())
                                            .grado(alumno.getGrado())
                                            .seccion(alumno.getSeccion())
                                            .activo(true)
                                            .build();
                                    asignacionesDefault.add(asignacion);
                                }
                            }
                        }
                    }
                }
                
                if (!asignacionesDefault.isEmpty()) {
                    asignacionRepo.saveAll(asignacionesDefault);
                    log.info("{} asignaciones automáticas creadas basadas en alumnos importados", asignacionesDefault.size());
                } else {
                    log.info("No se crearon asignaciones. Verifica que existan alumnos importados en la base de datos");
                }
            }

            log.info("DataInitializer completado - I.E.P. Nuestra Senora de Monserrat");
        };
    }
}