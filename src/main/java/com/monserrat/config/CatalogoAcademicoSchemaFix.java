package com.monserrat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CatalogoAcademicoSchemaFix implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("""
                    DO $$
                    DECLARE
                        item record;
                    BEGIN
                        FOR item IN
                            SELECT conname
                            FROM pg_constraint
                            WHERE conrelid = 'catalogos_academicos'::regclass
                              AND contype = 'u'
                              AND array_length(conkey, 1) = 1
                              AND conkey[1] = (
                                  SELECT attnum
                                  FROM pg_attribute
                                  WHERE attrelid = 'catalogos_academicos'::regclass
                                    AND attname = 'codigo'
                              )
                        LOOP
                            EXECUTE format('ALTER TABLE catalogos_academicos DROP CONSTRAINT IF EXISTS %I', item.conname);
                        END LOOP;

                        FOR item IN
                            SELECT indexname
                            FROM pg_indexes
                            WHERE schemaname = current_schema()
                              AND tablename = 'catalogos_academicos'
                              AND indexdef ILIKE 'CREATE UNIQUE INDEX%'
                              AND indexdef ILIKE '%(codigo)%'
                        LOOP
                            EXECUTE format('DROP INDEX IF EXISTS %I', item.indexname);
                        END LOOP;

                        IF NOT EXISTS (
                            SELECT 1
                            FROM pg_constraint
                            WHERE conname = 'uk_catalogo_academico_tipo_nivel_codigo'
                              AND conrelid = 'catalogos_academicos'::regclass
                        ) THEN
                            ALTER TABLE catalogos_academicos
                                ADD CONSTRAINT uk_catalogo_academico_tipo_nivel_codigo
                                UNIQUE (tipo, nivel, codigo);
                        END IF;
                    END $$;
                    """);
        } catch (Exception ex) {
            log.warn("No se pudo ajustar las restricciones de catalogos_academicos: {}", ex.getMessage());
        }
    }
}
