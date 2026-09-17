package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.config.ShellCaseWireMockStubs;
import uk.gov.hmcts.reform.ccd.data.ShellCaseScenarios.ShellCaseScenario;
import uk.gov.hmcts.reform.ccd.data.TestDataProvider;
import uk.gov.hmcts.reform.ccd.shell.service.ShellMappingService;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.config.ShellCaseWireMockStubs.ORIGINAL_CASE_REFERENCE;
import static uk.gov.hmcts.reform.ccd.config.ShellCaseWireMockStubs.ORIGINAL_CASE_TYPE;

@SpringBootTest(properties = {
    "features.shell-case.enabled=true",
    "remote.ccd-document-am.host=http://localhost:4603"
})
@ActiveProfiles("integration")
@ComponentScan("uk.gov.hmcts.reform.ccd")
@Execution(ExecutionMode.SAME_THREAD)
class ShellCaseDeletionIntegrationTest extends TestDataProvider {

    private static final String SCENARIO_SCRIPT = "scenarios/shell/SH-001-shell-case.sql";
    private static final List<Long> INITIAL_ROW_IDS = List.of(1L);
    private static final Map<String, List<Long>> INDEXED_DATA =
        Map.of(ORIGINAL_CASE_TYPE, List.of(ORIGINAL_CASE_REFERENCE));

    @Autowired
    private ApplicationExecutor executor;

    @Autowired
    private ProcessedCasesRecordHolder processedCasesRecordHolder;

    @Autowired
    private ShellMappingService shellMappingService;

    @Autowired
    private ShellCaseWireMockStubs shellCaseStubs;

    @BeforeEach
    void resetSharedState() {
        WIREMOCK_SERVER.resetAll();
        processedCasesRecordHolder.clearState();
        shellMappingService.clearCache();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("uk.gov.hmcts.reform.ccd.data.ShellCaseScenarios#provideShellCaseScenarios")
    void shouldOnlyDeleteOriginalCaseAfterShellCaseCreation(ShellCaseScenario scenario) throws Exception {
        setupData(ORIGINAL_CASE_TYPE, null, SCENARIO_SCRIPT, INITIAL_ROW_IDS, INDEXED_DATA);
        shellCaseStubs.setUp(WIREMOCK_SERVER, scenario.result());

        executor.execute();

        if (scenario.deletionExpected()) {
            verifySuccessfulShellCreationAndDeletion();
        } else {
            verifyShellFailurePreventedDeletion();
        }
    }

    private void verifySuccessfulShellCreationAndDeletion() {
        verifyDatabaseDeletion(List.of());
        verifyElasticsearchDeletion(
            Map.of(ORIGINAL_CASE_TYPE, List.of(ORIGINAL_CASE_REFERENCE)),
            Map.of(ORIGINAL_CASE_TYPE, List.of())
        );
        verifyRemoteDeletion(List.of(ORIGINAL_CASE_REFERENCE));
        assertThat(processedCasesRecordHolder.getFailedToDeleteCaseRefs()).isEmpty();

        WIREMOCK_SERVER.verify(1, postRequestedFor(urlPathEqualTo(shellCaseStubs.createCasePath()))
            .withRequestBody(equalToJson(expectedShellCasePayload(), true, true)));
    }

    private void verifyShellFailurePreventedDeletion() {
        verifyDatabaseDeletion(INITIAL_ROW_IDS);
        verifyElasticsearchDeletion(
            Map.of(ORIGINAL_CASE_TYPE, List.of()),
            INDEXED_DATA
        );
        assertThat(processedCasesRecordHolder.getFailedToDeleteCaseRefs())
            .containsExactly(ORIGINAL_CASE_REFERENCE);

        WIREMOCK_SERVER.verify(0, postRequestedFor(urlPathEqualTo("/documents/delete")));
        WIREMOCK_SERVER.verify(0, postRequestedFor(urlPathEqualTo("/am/role-assignments/query/delete")));
        WIREMOCK_SERVER.verify(0, postRequestedFor(urlPathEqualTo("/task/delete")));
    }

    private String expectedShellCasePayload() {
        return """
            {
              "data": {
                "archivedApplicant": {
                  "name": "Jane Smith"
                },
                "archivedDocument": {
                  "document_url": "http://document-store/documents/11111111-1111-1111-1111-111111111111",
                  "document_binary_url":
                    "http://document-store/documents/11111111-1111-1111-1111-111111111111/binary",
                  "document_filename": "evidence.pdf",
                  "document_hash": "document-hash-token"
                },
                "original_case_reference": "1504259907353529",
                "original_case_type": "FT_MasterCaseType"
              },
              "event_token": "create-event-token",
              "event": {
                "id": "createCase",
                "summary": "Create case",
                "description": "Initial case creation"
              },
              "ignore_warning": false
            }
            """;
    }
}
