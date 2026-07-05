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
}
