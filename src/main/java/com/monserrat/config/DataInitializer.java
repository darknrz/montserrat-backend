package com.monserrat.config;

import com.monserrat.entity.Admin;
import com.monserrat.entity.AsignacionAcademica;
import com.monserrat.entity.ChatbotFaq;
import com.monserrat.entity.Ingreso;
import com.monserrat.entity.Institution;
import com.monserrat.entity.RedSocial;
import com.monserrat.entity.RolUsuario;
import com.monserrat.entity.UsuarioAcademico;
import com.monserrat.entity.Video;
import com.monserrat.repository.AdminRepository;
import com.monserrat.repository.AsignacionAcademicaRepository;
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

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initData(
            AdminRepository adminRepo,
            InstitutionRepository institutionRepo,
            IngresoRepository ingresoRepo,
            VideoRepository videoRepo,
            RedSocialRepository redSocialRepo,
            UsuarioAcademicoRepository usuarioAcademicoRepo,
            AsignacionAcademicaRepository asignacionRepo,
            ChatbotFaqRepository chatbotFaqRepo) {

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

            if (!usuarioAcademicoRepo.existsByDni("12345678")) {
                usuarioAcademicoRepo.save(UsuarioAcademico.builder()
                        .dni("12345678")
                        .password(passwordEncoder.encode("12345678"))
                        .nombre("Docente Demo")
                        .rol(RolUsuario.DOCENTE)
                        .materia("Matematica")
                        .especialidad("Matematica")
                        .telefono("900000001")
                        .debeCambiarContrasena(true)
                        .activo(true)
                        .estado(com.monserrat.entity.EstadoUsuario.ACTIVO)
                        .build());
                log.info("Docente demo creado con DNI 12345678");
            }

            if (!usuarioAcademicoRepo.existsByDni("87654321")) {
                usuarioAcademicoRepo.save(UsuarioAcademico.builder()
                        .dni("87654321")
                        .password(passwordEncoder.encode("87654321"))
                        .nombre("Alumno Demo")
                        .rol(RolUsuario.ALUMNO)
                        .nivelEducativo(com.monserrat.entity.NivelEducativo.SECUNDARIA)
                        .grado(com.monserrat.entity.Grado.QUINTO_SECUNDARIA)
                        .seccion(com.monserrat.entity.Seccion.A)
                        .telefono("900000002")
                        .estado(com.monserrat.entity.EstadoUsuario.ACTIVO)
                        .estadoMatricula(com.monserrat.entity.EstadoMatricula.MATRICULADO)
                        .pensionPagada(false)
                        .pensionObservacion("Pendiente de regularizacion")
                        .debeCambiarContrasena(true)
                        .activo(true)
                        .build());
                log.info("Alumno demo creado con DNI 87654321");
            }

            if (!usuarioAcademicoRepo.existsByDni("11223344")) {
                usuarioAcademicoRepo.save(UsuarioAcademico.builder()
                        .dni("11223344")
                        .password(passwordEncoder.encode("11223344"))
                        .nombre("Alumno Primaria Demo")
                        .rol(RolUsuario.ALUMNO)
                        .nivelEducativo(com.monserrat.entity.NivelEducativo.PRIMARIA)
                        .grado(com.monserrat.entity.Grado.PRIMERO_PRIMARIA)
                        .seccion(com.monserrat.entity.Seccion.A)
                        .telefono("900000003")
                        .estado(com.monserrat.entity.EstadoUsuario.ACTIVO)
                        .estadoMatricula(com.monserrat.entity.EstadoMatricula.MATRICULADO)
                        .pensionPagada(false)
                        .pensionObservacion("Pendiente de regularizacion")
                        .debeCambiarContrasena(true)
                        .activo(true)
                        .build());
                log.info("Alumno primaria demo creado con DNI 11223344");
            }

            if (!usuarioAcademicoRepo.existsByDni("22334455")) {
                usuarioAcademicoRepo.save(UsuarioAcademico.builder()
                        .dni("22334455")
                        .password(passwordEncoder.encode("22334455"))
                        .nombre("Docente Primaria Demo")
                        .rol(RolUsuario.DOCENTE)
                        .telefono("900000004")
                        .debeCambiarContrasena(true)
                        .activo(true)
                        .estado(com.monserrat.entity.EstadoUsuario.ACTIVO)
                        .build());
                log.info("Docente primaria demo creado con DNI 22334455");
            }

            UsuarioAcademico docenteDemo = usuarioAcademicoRepo.findByDni("12345678").orElse(null);
            UsuarioAcademico alumnoDemo = usuarioAcademicoRepo.findByDni("87654321").orElse(null);
            UsuarioAcademico docentePrimariaDemo = usuarioAcademicoRepo.findByDni("22334455").orElse(null);
            UsuarioAcademico alumnoPrimariaDemo = usuarioAcademicoRepo.findByDni("11223344").orElse(null);
            if (docenteDemo != null && alumnoDemo != null && docentePrimariaDemo != null && alumnoPrimariaDemo != null && asignacionRepo.count() == 0) {
                asignacionRepo.save(AsignacionAcademica.builder()
                        .docente(docenteDemo)
                        .alumno(alumnoDemo)
                        .curso(com.monserrat.entity.CursoAcademico.MATEMATICA)
                        .nivelEducativo(com.monserrat.entity.NivelEducativo.SECUNDARIA)
                        .grado(com.monserrat.entity.Grado.QUINTO_SECUNDARIA)
                        .seccion(com.monserrat.entity.Seccion.A)
                        .activo(true)
                        .build());

                asignacionRepo.save(AsignacionAcademica.builder()
                        .docente(docentePrimariaDemo)
                        .alumno(alumnoPrimariaDemo)
                        .curso(com.monserrat.entity.CursoAcademico.MATEMATICA)
                        .nivelEducativo(com.monserrat.entity.NivelEducativo.PRIMARIA)
                        .grado(com.monserrat.entity.Grado.PRIMERO_PRIMARIA)
                        .seccion(com.monserrat.entity.Seccion.A)
                        .activo(true)
                        .build());

                log.info("Asignaciones academicas demo creadas");
            }

            log.info("DataInitializer completado - I.E.P. Nuestra Senora de Monserrat");
        };
    }
}

