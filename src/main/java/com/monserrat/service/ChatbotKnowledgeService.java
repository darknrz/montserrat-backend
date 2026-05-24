package com.monserrat.service;

import com.monserrat.entity.ChatbotFaq;
import com.monserrat.entity.Ingreso;
import com.monserrat.entity.Institution;
import com.monserrat.entity.RedSocial;
import com.monserrat.entity.Video;
import com.monserrat.repository.ChatbotFaqRepository;
import com.monserrat.repository.IngresoRepository;
import com.monserrat.repository.InstitutionRepository;
import com.monserrat.repository.RedSocialRepository;
import com.monserrat.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatbotKnowledgeService {

    private final InstitutionRepository institutionRepository;
    private final IngresoRepository ingresoRepository;
    private final VideoRepository videoRepository;
    private final RedSocialRepository redSocialRepository;
    private final ChatbotFaqRepository faqRepository;

    public String buildContext() {
        StringBuilder context = new StringBuilder();

        institutionRepository.findAll().stream().findFirst().ifPresent(institution -> appendInstitution(context, institution));
        appendIngresantes(context, ingresoRepository.findByActivoTrue());
        appendVideos(context, videoRepository.findByActivoTrueOrderByOrdenAsc());
        appendRedes(context, redSocialRepository.findByActivoTrueOrderByOrdenAsc());
        appendFaqs(context, faqRepository.findByActivoTrueOrderByOrdenAsc());

        return context.toString();
    }

    private void appendInstitution(StringBuilder context, Institution institution) {
        context.append("DATOS INSTITUCIONALES\n")
                .append("Nombre: ").append(institution.getNombre()).append('\n')
                .append("Direccion: ").append(institution.getDireccion()).append(", ").append(institution.getCiudad()).append('\n')
                .append("Telefono: ").append(institution.getTelefono()).append('\n')
                .append("Correo: ").append(institution.getEmail()).append('\n')
                .append("Horario: ").append(institution.getHorarioAtencion()).append('\n')
                .append("Niveles: ").append(institution.getNiveles()).append('\n')
                .append("Tipo: ").append(institution.getTipo()).append('\n')
                .append("Mision: ").append(institution.getMision()).append('\n')
                .append("Vision: ").append(institution.getVision()).append("\n\n");
    }

    private void appendIngresantes(StringBuilder context, List<Ingreso> ingresantes) {
        context.append("INGRESANTES\n");
        ingresantes.forEach(i -> context.append("- ")
                .append(i.getNombre()).append(" | ")
                .append(i.getUniversidadSiglas()).append(" | ")
                .append(i.getCarrera()).append(" | ")
                .append(i.getAnio()).append(" | ")
                .append(i.getTipoSeleccion()).append('\n'));
        context.append('\n');
    }

    private void appendVideos(StringBuilder context, List<Video> videos) {
        context.append("VIDEOS\n");
        videos.forEach(v -> context.append("- ")
                .append(v.getTitulo()).append(" | ")
                .append(v.getTag()).append(" | Tipo: ")
                .append(v.getMediaType()).append(" | URL: ")
                .append(v.getMediaUrl()).append('\n'));
        context.append('\n');
    }

    private void appendRedes(StringBuilder context, List<RedSocial> redes) {
        context.append("REDES SOCIALES\n");
        redes.forEach(r -> context.append("- ")
                .append(r.getNombre()).append(": ")
                .append(r.getUrl()).append('\n'));
        context.append('\n');
    }

    private void appendFaqs(StringBuilder context, List<ChatbotFaq> faqs) {
        context.append("PREGUNTAS FRECUENTES\n");
        faqs.forEach(f -> context.append("- Pregunta: ")
                .append(f.getPregunta()).append(" | Respuesta: ")
                .append(f.getRespuesta()).append('\n'));
    }
}
