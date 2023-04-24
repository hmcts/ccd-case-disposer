package uk.gov.hmcts.reform.ccd.utils;

import jakarta.inject.Inject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.data.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.with;

@Component
public class DatabaseIntegrationTestUtils {

    @Inject
    private DataSource dataSource;

    @Inject
    private CaseDataRepository caseDataRepository;

    public void verifyDatabaseIsPopulated(final List<Long> rowIds) {
        rowIds.forEach(item -> {
            Optional<CaseDataEntity> caseDataToDelete = caseDataRepository.findById(item);
            assertThat(caseDataToDelete).isPresent();
        });
    }

    public void verifyDatabaseDeletion(final List<Long> rowIds) {
        with().await()
                .untilAsserted(() -> {
                    final List<CaseDataEntity> all = caseDataRepository.findAll();
                    final List<Long> actualRowIds = all.stream()
                            .map(CaseDataEntity::getId)
                            .toList();

                    assertThat(actualRowIds)
                            .isNotNull()
                            .containsExactlyInAnyOrderElementsOf(rowIds);
                });
    }

    public void insertDataIntoDatabase(final String scriptPath) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            final ClassPathResource resource = new ClassPathResource(scriptPath);
            ScriptUtils.executeSqlScript(connection, resource);
        }
    }
}
