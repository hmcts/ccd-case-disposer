package uk.gov.hmcts.reform.ccd.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.data.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class DatabaseTestUtils {

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
        final List<CaseDataEntity> all = caseDataRepository.findAll();
        final List<Long> actualRowIds = all.stream()
                .map(CaseDataEntity::getId)
                .collect(Collectors.toUnmodifiableList());

        assertThat(actualRowIds)
                .isNotNull()
                .containsExactlyInAnyOrderElementsOf(rowIds);
    }

    public void insertDataIntoDatabase(final String scriptPath) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            final ClassPathResource resource = new ClassPathResource(scriptPath);
            ScriptUtils.executeSqlScript(connection, resource);
        }
    }
}
