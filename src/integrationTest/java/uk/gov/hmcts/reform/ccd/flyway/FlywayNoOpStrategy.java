package uk.gov.hmcts.reform.ccd.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import uk.gov.hmcts.reform.ccd.exception.MigrationScriptException;

import java.util.stream.Stream;

@SuppressWarnings({"PMD.DataflowAnomalyAnalysis"})
public class FlywayNoOpStrategy implements FlywayMigrationStrategy {

    @Override
    public void migrate(final Flyway flyway) {
        Stream.of(flyway.info().all())
                .filter(info -> !info.getState().isApplied())
                .findFirst()
                .ifPresent(info -> {
                    throw new MigrationScriptException(info.getScript());
                });
    }
}