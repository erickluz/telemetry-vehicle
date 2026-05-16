package org.erick.telemetrydlqservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TelemetryDlqSchemaMigration implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelemetryDlqSchemaMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public TelemetryDlqSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!isOidStackTraceColumn()) {
            return;
        }

        try {
            jdbcTemplate.execute("""
                    alter table telemetry_dlq_records
                    alter column stack_trace type text
                    using case
                        when stack_trace is null then null
                        else convert_from(lo_get(stack_trace), 'UTF8')
                    end
                    """);
            LOGGER.info("Coluna telemetry_dlq_records.stack_trace convertida de oid para text.");
        } catch (RuntimeException ex) {
            LOGGER.warn("Nao foi possivel converter stack_trace preservando Large Object. Convertendo oid para texto.", ex);
            jdbcTemplate.execute("""
                    alter table telemetry_dlq_records
                    alter column stack_trace type text
                    using stack_trace::text
                    """);
        }
    }

    private boolean isOidStackTraceColumn() {
        try {
            String dataType = jdbcTemplate.queryForObject("""
                    select udt_name
                    from information_schema.columns
                    where table_schema = current_schema()
                      and table_name = 'telemetry_dlq_records'
                      and column_name = 'stack_trace'
                    """, String.class);
            return "oid".equalsIgnoreCase(dataType);
        } catch (EmptyResultDataAccessException ex) {
            return false;
        }
    }
}
