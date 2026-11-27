package uk.gov.hmcts.reform.ccd.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.data.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.model.DocumentField;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.with;

@Component
public class DatabaseTestUtils {

    private final ObjectMapper objectMapper = new ObjectMapper();

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

                    assertThat(databaseEndStateRowIds).hasSameSizeAs(endStateRows);
                    assertThat(databaseEndStateRowIds).containsExactlyInAnyOrderElementsOf(endStateRows);
                });
    }

    public CaseDataEntity verifyShellCaseCreation(String shellCaseType, List<Long> endStateRows) {
        AtomicReference<List<CaseDataEntity>> result = new AtomicReference<>();

        with().await().untilAsserted(() -> {
            List<CaseDataEntity> cases =
                caseDataRepository.findByCaseType(shellCaseType);

            assertThat(cases).hasSameSizeAs(endStateRows);
            result.set(cases);
        });

        return result.get().getFirst();
    }

    public void insertDataIntoDatabase(final String scriptPath) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            final ClassPathResource resource = new ClassPathResource(scriptPath);
            ScriptUtils.executeSqlScript(connection, resource);
        }
    }

    public void insertDataIntoDatabase(
        String scriptPath,
        DocumentField documentField
    ) throws SQLException, IOException {
        try (Connection connection = dataSource.getConnection()) {
            ClassPathResource resource = new ClassPathResource(scriptPath);
            String sql = new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
            );

            String documentJson = objectMapper.writeValueAsString(documentField);
            sql = sql.replace("DOCUMENT_FIELD_DATA", documentJson);

            ScriptUtils.executeSqlScript(
                connection,
                new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8))
            );
        }
    }
}
