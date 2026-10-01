package com.monserrat.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSchemaUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSchemaUpdater.class);
    private static final int MIN_CATALOGO_NOMBRE_LENGTH = 500;

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void updateSchema() {
        ensureUsuariosAcademicosCodigoChatbot();
        dropEnumCheckConstraints();

        try {
            Integer length = jdbcTemplate.queryForObject(
                    "SELECT character_maximum_length FROM information_schema.columns " +
                    "WHERE table_name = ? AND column_name = ? AND table_schema = 'public'",
                    Integer.class,
                    "catalogos_academicos",
                    "nombre"
            );

            if (length != null && length < MIN_CATALOGO_NOMBRE_LENGTH) {
                LOGGER.info("Updating catalogos_academicos.nombre length from {} to {}", length, MIN_CATALOGO_NOMBRE_LENGTH);
                jdbcTemplate.execute(
                        "ALTER TABLE catalogos_academicos ALTER COLUMN nombre TYPE varchar(" + MIN_CATALOGO_NOMBRE_LENGTH + ")"
                );
            }
        } catch (Exception ex) {
            LOGGER.debug("Database schema update skipped or failed: {}", ex.getMessage());
        }
    }

    private void ensureUsuariosAcademicosCodigoChatbot() {
        try {
            Boolean exists = jdbcTemplate.queryForObject(
                    "SELECT EXISTS (" +
                            "SELECT 1 FROM information_schema.columns " +
                            "WHERE table_name = ? AND column_name = ? AND table_schema = 'public'" +
                            ")",
                    Boolean.class,
                    "usuarios_academicos",
                    "codigo_chatbot"
            );

            if (!Boolean.TRUE.equals(exists)) {
                LOGGER.info("Adding usuarios_academicos.codigo_chatbot column");
                jdbcTemplate.execute("ALTER TABLE usuarios_academicos ADD COLUMN codigo_chatbot varchar(12)");
                jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_usuarios_academicos_codigo_chatbot ON usuarios_academicos (codigo_chatbot)");
            }
        } catch (Exception ex) {
            LOGGER.debug("codigo_chatbot schema update skipped or failed: {}", ex.getMessage());
        }
    }

    /**
     * Hibernate 6 crea CHECK constraints para columnas enum (nivel_educativo, grado). Al agregar
     * INICIAL a NivelEducativo/Grado, esos checks (creados con la lista antigua) rechazarian el nuevo valor.
     * Se eliminan solo los checks que mencionan esas columnas; los datos existentes no se modifican.
     */
    private void dropEnumCheckConstraints() {
        try {
            var rows = jdbcTemplate.queryForList(
                    "SELECT c.conrelid::regclass::text AS tabla, c.conname AS nombre " +
                            "FROM pg_constraint c " +
                            "WHERE c.contype = 'c' " +
                            "AND c.conrelid::regclass::text IN ('usuarios_academicos', 'asignaciones_academicas') " +
                            "AND (pg_get_constraintdef(c.oid) ILIKE '%nivel_educativo%' " +
                            "OR pg_get_constraintdef(c.oid) ILIKE '%grado%')");
            for (var row : rows) {
                String tabla = String.valueOf(row.get("tabla"));
                String nombre = String.valueOf(row.get("nombre"));
                LOGGER.info("Dropping enum check constraint {} on {}", nombre, tabla);
                jdbcTemplate.execute("ALTER TABLE " + tabla + " DROP CONSTRAINT IF EXISTS \"" + nombre + "\"");
            }
        } catch (Exception ex) {
            LOGGER.debug("Enum check constraint cleanup skipped or failed: {}", ex.getMessage());
        }
    }
}
