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

            // Crear áreas curriculares y competencias de PRIMARIA
            // Verificar si ya existe C1 (competencia marker)
            boolean competenciasExisten = catalogoRepo.findAll().stream()
                    .anyMatch(c -> "C1".equals(c.getCodigo()) && "PRIMARIA".equals(c.getNivel()));
            
            if (!competenciasExisten) {
                log.info("Iniciando creación de áreas curriculares y competencias de PRIMARIA...");
                
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
                        CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C28").nombre("Gestiona su aprendizaje de manera autónoma.").activo(true).orden(28).build()
                );
                catalogoRepo.saveAll(competenciasPrimaria);
                log.info("{} competencias de primaria creadas", competenciasPrimaria.size());
            } else {
                log.info("Áreas curriculares y competencias de PRIMARIA ya existen");
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

            log.info("DataInitializer completado - I.E.P. Nuestra Senora de Monserrat");
        };
    }
}