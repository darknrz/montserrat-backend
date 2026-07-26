package com.monserrat.service;

import com.monserrat.dto.chatbot.ChatbotSocketResponse;
import com.monserrat.entity.AsignacionAcademica;
import com.monserrat.entity.AsistenciaAcademica;
import com.monserrat.entity.ChatbotConversation;
import com.monserrat.entity.ChatbotMessage;
import com.monserrat.entity.CatalogoAcademico;
import com.monserrat.entity.CursoAcademico;
import com.monserrat.entity.EstadoAsistencia;
import com.monserrat.entity.Grado;
import com.monserrat.entity.NivelEducativo;
import com.monserrat.entity.NotaAcademica;
import com.monserrat.entity.PensionMensual;
import com.monserrat.entity.RolUsuario;
import com.monserrat.entity.Seccion;
import com.monserrat.entity.UsuarioAcademico;
import com.monserrat.repository.AsignacionAcademicaRepository;
import com.monserrat.repository.AsistenciaAcademicaRepository;
import com.monserrat.repository.CatalogoAcademicoRepository;
import com.monserrat.repository.ChatbotConversationRepository;
import com.monserrat.repository.ChatbotLeadRepository;
import com.monserrat.repository.ChatbotMessageRepository;
import com.monserrat.repository.NotaAcademicaRepository;
import com.monserrat.repository.PensionMensualRepository;
import com.monserrat.repository.UsuarioAcademicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private ChatbotConversationRepository conversationRepository;

    @Mock
    private ChatbotMessageRepository messageRepository;

    @Mock
    private ChatbotLeadRepository leadRepository;

    @Mock
    private ChatbotKnowledgeService knowledgeService;

    @Mock
    private GroqAiService groqAiService;

    @Mock
    private UsuarioAcademicoRepository usuarioAcademicoRepository;

    @Mock
    private NotaAcademicaRepository notaAcademicaRepository;

    @Mock
    private AsistenciaAcademicaRepository asistenciaAcademicaRepository;

    @Mock
    private PensionMensualRepository pensionMensualRepository;

    @Mock
    private AsignacionAcademicaRepository asignacionAcademicaRepository;

    @Mock
    private CatalogoAcademicoRepository catalogoAcademicoRepository;

    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        chatbotService = new ChatbotService(
                conversationRepository,
                messageRepository,
                leadRepository,
                knowledgeService,
                groqAiService,
                new ChatbotGuardService(),
                usuarioAcademicoRepository,
                notaAcademicaRepository,
                asistenciaAcademicaRepository,
                pensionMensualRepository,
                asignacionAcademicaRepository,
                catalogoAcademicoRepository
        );
    }

    @Test
    void processUserMessageDoesNotCallAiForNoise() {
        ChatbotConversation conversation = ChatbotConversation.builder()
                .id(1L)
                .canal("WEB")
                .estado("ABIERTA")
                .build();
        AtomicLong ids = new AtomicLong(10);

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(ChatbotMessage.class))).thenAnswer(invocation -> {
            ChatbotMessage saved = invocation.getArgument(0);
            saved.setId(ids.getAndIncrement());
            return saved;
        });

        List<ChatbotSocketResponse> responses = chatbotService.processUserMessage(1L, "asdfgh ???");

        assertThat(responses).hasSize(2);
        assertThat(responses.get(1).getText()).contains("No te entendi bien");
        assertThat(responses.get(1).getIntent()).isEqualTo("NO_ENTENDIDO");
        verifyNoInteractions(knowledgeService, groqAiService);
        verify(leadRepository, never()).save(any());
    }

    @Test
    void processUserMessageStoresNameAndGreetsVisitor() {
        ChatbotConversation conversation = ChatbotConversation.builder()
                .id(1L)
                .canal("WEB")
                .estado("ABIERTA")
                .build();
        AtomicLong ids = new AtomicLong(20);

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(messageRepository.save(any(ChatbotMessage.class))).thenAnswer(invocation -> {
            ChatbotMessage saved = invocation.getArgument(0);
            saved.setId(ids.getAndIncrement());
            return saved;
        });

        List<ChatbotSocketResponse> responses = chatbotService.processUserMessage(1L, "me llamo elvis");

        assertThat(conversation.getNombreVisitante()).isEqualTo("Elvis");
        assertThat(responses.get(1).getText()).contains("Hola **Elvis**");
        assertThat(responses.get(1).getIntent()).isEqualTo("PRESENTACION");
        verifyNoInteractions(knowledgeService, groqAiService);
    }

    @Test
    void processUserMessageUsesPreviousIntentForYearFollowUp() {
        ChatbotConversation conversation = ChatbotConversation.builder()
                .id(1L)
                .canal("WEB")
                .estado("ABIERTA")
                .build();
        ChatbotMessage previousUserMessage = ChatbotMessage.builder()
                .id(1L)
                .conversacion(conversation)
                .emisor("USER")
                .mensaje("ingresantes")
                .intencion("INGRESANTES")
                .build();
        AtomicLong ids = new AtomicLong(30);

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversacionIdOrderByCreadoEnAsc(1L)).thenReturn(List.of(previousUserMessage));
        when(messageRepository.save(any(ChatbotMessage.class))).thenAnswer(invocation -> {
            ChatbotMessage saved = invocation.getArgument(0);
            saved.setId(ids.getAndIncrement());
            return saved;
        });
        when(knowledgeService.buildContext()).thenReturn("""
                INGRESANTES
                - Ana Torres | UNCP | Medicina | 2025 | 1ra Seleccion
                - Maria Quispe | UNI | Derecho | 2024 | Ordinario
                """);
        when(groqAiService.answer(any(), any(), any(), any())).thenCallRealMethod();

        List<ChatbotSocketResponse> responses = chatbotService.processUserMessage(1L, "en el ano 2025");

        assertThat(responses.get(1).getIntent()).isEqualTo("INGRESANTES");
        assertThat(responses.get(1).getText()).contains("Ingresantes registrados en 2025:");
        assertThat(responses.get(1).getText()).contains("Ana Torres");
        assertThat(responses.get(1).getText()).doesNotContain("Maria Quispe");
        verifyNoMoreInteractions(leadRepository);
    }

    @Test
    void processUserMessageBlocksPersonalNotesWithoutVerification() {
        ChatbotConversation conversation = ChatbotConversation.builder()
                .id(1L)
                .canal("WEB")
                .estado("ABIERTA")
                .build();
        AtomicLong ids = new AtomicLong(40);

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(ChatbotMessage.class))).thenAnswer(invocation -> {
            ChatbotMessage saved = invocation.getArgument(0);
            saved.setId(ids.getAndIncrement());
            return saved;
        });

        List<ChatbotSocketResponse> responses = chatbotService.processUserMessage(1L, "quiero ver mis notas");

        assertThat(responses.get(1).getIntent()).isEqualTo("PERSONAL_NOTAS");
        assertThat(responses.get(1).getText()).contains("necesito verificar");
        assertThat(responses.get(1).getText()).contains("Nombre completo, DNI o codigo institucional");
        verifyNoInteractions(knowledgeService, groqAiService, notaAcademicaRepository);
    }

    @Test
    void processUserMessageAcceptsDniAndChatbotCode() {
        ChatbotConversation conversation = ChatbotConversation.builder()
                .id(1L)
                .canal("WEB")
                .estado("ABIERTA")
                .build();
        ChatbotMessage previousUserMessage = ChatbotMessage.builder()
                .id(1L)
                .conversacion(conversation)
                .emisor("USER")
                .mensaje("quiero consultar mis notas")
                .intencion("PERSONAL_NOTAS")
                .build();
        UsuarioAcademico alumno = UsuarioAcademico.builder()
                .dni("71234567")
                .codigo("2026001A")
                .codigoChatbot("ABCD2345")
                .nombre("Juan Carlos Perez Gomez")
                .rol(RolUsuario.ALUMNO)
                .activo(true)
                .build();
        AtomicLong ids = new AtomicLong(50);

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversacionIdOrderByCreadoEnAsc(1L)).thenReturn(List.of(previousUserMessage));
        when(usuarioAcademicoRepository.findByCodigoChatbotIgnoreCase(anyString())).thenAnswer(invocation ->
                "ABCD2345".equalsIgnoreCase(invocation.getArgument(0)) ? Optional.of(alumno) : Optional.empty());
        when(notaAcademicaRepository.findByAlumno_DniOrderByPeriodoDescCreatedAtDesc("71234567")).thenReturn(List.of());
        when(groqAiService.generate(anyString(), anyString())).thenReturn("Verificacion correcta. Aun no hay notas registradas para **Juan Carlos Perez Gomez**.");
        when(messageRepository.save(any(ChatbotMessage.class))).thenAnswer(invocation -> {
            ChatbotMessage saved = invocation.getArgument(0);
            saved.setId(ids.getAndIncrement());
            return saved;
        });

        List<ChatbotSocketResponse> responses = chatbotService.processUserMessage(
                1L,
                "dni: 71234567 y codigo del chatbot: ABCD2345"
        );

        assertThat(responses.get(1).getIntent()).isEqualTo("PERSONAL_NOTAS");
        assertThat(responses.get(1).getText()).contains("Verificacion correcta");
        assertThat(responses.get(1).getText()).contains("Aun no hay notas registradas");
        verifyNoInteractions(knowledgeService);
    }

    @Test
    void processUserMessageFormatsNotesWithAreaAndCompetenceNames() {
        ChatbotConversation conversation = ChatbotConversation.builder()
                .id(1L)
                .canal("WEB")
                .estado("ABIERTA")
                .build();
        UsuarioAcademico alumno = UsuarioAcademico.builder()
                .dni("71234567")
                .codigo("2026001A")
                .codigoChatbot("ABCD2345")
                .nombre("Juan Carlos Perez Gomez")
                .rol(RolUsuario.ALUMNO)
                .nivelEducativo(NivelEducativo.PRIMARIA)
                .activo(true)
                .build();
        NotaAcademica nota = NotaAcademica.builder()
                .alumno(alumno)
                .curso(CursoAcademico.INGLES)
                .periodo("BIMESTRE_4")
                .competenciaId("C18")
                .valor(3.0)
                .build();
        AtomicLong ids = new AtomicLong(60);

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(usuarioAcademicoRepository.findByCodigoChatbotIgnoreCase(anyString())).thenAnswer(invocation ->
                "ABCD2345".equalsIgnoreCase(invocation.getArgument(0)) ? Optional.of(alumno) : Optional.empty());
        when(notaAcademicaRepository.findByAlumno_DniOrderByPeriodoDescCreatedAtDesc("71234567")).thenReturn(List.of(nota));
        when(catalogoAcademicoRepository.findAllByOrderByOrdenAscIdAsc()).thenReturn(List.of(
                CatalogoAcademico.builder().tipo("AREA_CURRICULAR").nivel("PRIMARIA").codigo("INGLES").nombre("Ingles").activo(true).orden(1).build(),
                CatalogoAcademico.builder().tipo("COMPETENCIA").nivel("PRIMARIA").codigo("C18").nombre("Lee diversos tipos de textos en ingles como lengua extranjera.").activo(true).orden(18).build()
        ));
        when(groqAiService.generate(anyString(), anyString())).thenReturn("""
                | Area curricular | Periodo | Competencia | Nota |
                |---|---|---|---|
                | Ingles | Bimestre 4 | Lee diversos tipos de textos en ingles como lengua extranjera. | A |
                """);
        when(messageRepository.save(any(ChatbotMessage.class))).thenAnswer(invocation -> {
            ChatbotMessage saved = invocation.getArgument(0);
            saved.setId(ids.getAndIncrement());
            return saved;
        });

        List<ChatbotSocketResponse> responses = chatbotService.processUserMessage(
                1L,
                "mis notas Juan Carlos Perez Gomez DNI 71234567 codigo 2026001A chatbot ABCD2345"
        );

        assertThat(responses.get(1).getText()).contains("| Area curricular | Periodo | Competencia | Nota |");
        assertThat(responses.get(1).getText()).contains("Ingles");
        assertThat(responses.get(1).getText()).contains("Lee diversos tipos de textos");
        assertThat(responses.get(1).getText()).contains("| A |");
    }

    @Test
    void processUserMessageReturnsVerifiedAttendance() {
        ChatbotConversation conversation = ChatbotConversation.builder()
                .id(1L)
                .canal("WEB")
                .estado("ABIERTA")
                .build();
        UsuarioAcademico alumno = UsuarioAcademico.builder()
                .dni("71234567")
                .codigo("2026001A")
                .codigoChatbot("ABCD2345")
                .nombre("Juan Carlos Perez Gomez")
                .rol(RolUsuario.ALUMNO)
                .activo(true)
                .build();
        AsistenciaAcademica asistencia = AsistenciaAcademica.builder()
                .alumno(alumno)
                .fecha(LocalDate.of(2026, 7, 20))
                .estado(EstadoAsistencia.PRESENTE)
                .observacion("Ingreso puntual")
                .build();
        AtomicLong ids = new AtomicLong(70);

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(usuarioAcademicoRepository.findByCodigoChatbotIgnoreCase(anyString())).thenAnswer(invocation ->
                "ABCD2345".equalsIgnoreCase(invocation.getArgument(0)) ? Optional.of(alumno) : Optional.empty());
        when(asistenciaAcademicaRepository.findByAlumno_DniOrderByFechaDesc("71234567")).thenReturn(List.of(asistencia));
        when(messageRepository.save(any(ChatbotMessage.class))).thenAnswer(invocation -> {
            ChatbotMessage saved = invocation.getArgument(0);
            saved.setId(ids.getAndIncrement());
            return saved;
        });

        List<ChatbotSocketResponse> responses = chatbotService.processUserMessage(
                1L,
                "mi asistencia dni 71234567 chatbot ABCD2345"
        );

        assertThat(responses.get(1).getIntent()).isEqualTo("PERSONAL_ASISTENCIA");
        assertThat(responses.get(1).getText()).contains("Verificacion correcta");
        assertThat(responses.get(1).getText()).contains("20/07/2026");
        assertThat(responses.get(1).getText()).contains("PRESENTE");
    }

    @Test
    void processUserMessageReturnsVerifiedPension() {
        ChatbotConversation conversation = ChatbotConversation.builder()
                .id(1L)
                .canal("WEB")
                .estado("ABIERTA")
                .build();
        UsuarioAcademico alumno = UsuarioAcademico.builder()
                .dni("71234567")
                .codigo("2026001A")
                .codigoChatbot("ABCD2345")
                .nombre("Juan Carlos Perez Gomez")
                .rol(RolUsuario.ALUMNO)
                .activo(true)
                .pensionPagada(false)
                .build();
        PensionMensual pension = PensionMensual.builder()
                .alumno(alumno)
                .anio(Year.now().getValue())
                .mes(7)
                .pagada(true)
                .observacion("Pagado en caja")
                .build();
        AtomicLong ids = new AtomicLong(80);

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(usuarioAcademicoRepository.findByCodigoChatbotIgnoreCase(anyString())).thenAnswer(invocation ->
                "ABCD2345".equalsIgnoreCase(invocation.getArgument(0)) ? Optional.of(alumno) : Optional.empty());
        when(pensionMensualRepository.findByAlumno_DniAndAnio("71234567", Year.now().getValue())).thenReturn(List.of(pension));
        when(messageRepository.save(any(ChatbotMessage.class))).thenAnswer(invocation -> {
            ChatbotMessage saved = invocation.getArgument(0);
            saved.setId(ids.getAndIncrement());
            return saved;
        });

        List<ChatbotSocketResponse> responses = chatbotService.processUserMessage(
                1L,
                "quiero ver mi pension dni 71234567 codigo del chatbot ABCD2345"
        );

        assertThat(responses.get(1).getIntent()).isEqualTo("PERSONAL_PENSION");
        assertThat(responses.get(1).getText()).contains("Verificacion correcta");
        assertThat(responses.get(1).getText()).contains("Pension actual");
        assertThat(responses.get(1).getText()).contains("Mes 7: pagada");
    }

    @Test
    void processUserMessageReturnsVerifiedCourses() {
        ChatbotConversation conversation = ChatbotConversation.builder()
                .id(1L)
                .canal("WEB")
                .estado("ABIERTA")
                .build();
        UsuarioAcademico alumno = UsuarioAcademico.builder()
                .dni("71234567")
                .codigo("2026001A")
                .codigoChatbot("ABCD2345")
                .nombre("Juan Carlos Perez Gomez")
                .rol(RolUsuario.ALUMNO)
                .activo(true)
                .build();
        UsuarioAcademico docente = UsuarioAcademico.builder()
                .dni("70111111")
                .nombre("Maria Torres")
                .rol(RolUsuario.DOCENTE)
                .activo(true)
                .build();
        AsignacionAcademica asignacion = AsignacionAcademica.builder()
                .alumno(alumno)
                .docente(docente)
                .curso(CursoAcademico.MATEMATICA)
                .nivelEducativo(NivelEducativo.PRIMARIA)
                .grado(Grado.SEXTO_PRIMARIA)
                .seccion(Seccion.A)
                .activo(true)
                .build();
        AtomicLong ids = new AtomicLong(90);

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(usuarioAcademicoRepository.findByCodigoChatbotIgnoreCase(anyString())).thenAnswer(invocation ->
                "ABCD2345".equalsIgnoreCase(invocation.getArgument(0)) ? Optional.of(alumno) : Optional.empty());
        when(asignacionAcademicaRepository.findByAlumno_DniAndActivoTrue("71234567")).thenReturn(List.of(asignacion));
        when(catalogoAcademicoRepository.findAllByOrderByOrdenAscIdAsc()).thenReturn(List.of(
                CatalogoAcademico.builder().tipo("AREA_CURRICULAR").nivel("PRIMARIA").codigo("MATEMATICA").nombre("Matematica").activo(true).orden(1).build()
        ));
        when(messageRepository.save(any(ChatbotMessage.class))).thenAnswer(invocation -> {
            ChatbotMessage saved = invocation.getArgument(0);
            saved.setId(ids.getAndIncrement());
            return saved;
        });

        List<ChatbotSocketResponse> responses = chatbotService.processUserMessage(
                1L,
                "mis cursos dni 71234567 chatbot ABCD2345"
        );

        assertThat(responses.get(1).getIntent()).isEqualTo("PERSONAL_CURSOS");
        assertThat(responses.get(1).getText()).contains("| Curso | Docente | Grado | Seccion |");
        assertThat(responses.get(1).getText()).contains("Matematica");
        assertThat(responses.get(1).getText()).contains("Maria Torres");
        assertThat(responses.get(1).getText()).contains("6to Primaria");
    }
}
