package uk.gov.hmcts.reform.ccd.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.data.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.with;

@Component
public class DatabaseTestUtils {

    @Inject
    private DataSource dataSource;

    @Inject
    private CaseDataRepository caseDataRepository;

    public void verifyDatabaseIsPopulated(final List<Long> rowIds) {
        rowIds.forEach(item -> {
            Optional<CaseDataEntity> caseDataToDelete = caseDataRepository.findByReference(item);
            assertThat(caseDataToDelete).isPresent();
        });
    }

    public void verifyDatabaseDeletion(final List<Long> initialRowIds,
                                       final List<Long> endStateRows) {
        with().await()
                .untilAsserted(() -> {
                    final List<Long> databaseEndStateRowIds = new ArrayList<>();
                    initialRowIds.forEach(item -> {
                        Optional<CaseDataEntity> caseDataToDelete = caseDataRepository.findByReference(item);
                        if (caseDataToDelete.isPresent()) {
                            databaseEndStateRowIds.add(caseDataToDelete.get().getReference());
                        }
                    });

                    assertThat(databaseEndStateRowIds.size())
                            .isEqualTo(endStateRows.size());
                    assertThat(databaseEndStateRowIds).containsExactlyInAnyOrderElementsOf(endStateRows);
                });
    }


    public void insertDataIntoDatabase(final String scriptPath) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            final ClassPathResource resource = new ClassPathResource(scriptPath);
            ScriptUtils.executeSqlScript(connection, resource);
        }
    }
}
