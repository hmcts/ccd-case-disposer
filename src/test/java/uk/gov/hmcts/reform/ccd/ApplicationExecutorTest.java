package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.exception.LogAndAuditException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionAsyncService;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionLoggingService;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionService;
import uk.gov.hmcts.reform.ccd.service.CaseFinderService;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;
import uk.gov.hmcts.reform.ccd.util.log.CaseFamiliesFilter;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA4_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class ApplicationExecutorTest {

    private static final LocalTime CUT_OFF_TIME = LocalTime.parse("06:00");

    @Mock
    private CaseFinderService caseFindingService;

    @Mock
    private CaseFamiliesFilter caseFamiliesFilter;

    @Mock
    private CaseDeletionService caseDeletionService;

    @Mock
    private CaseDeletionAsyncService caseDeletionAsyncService;

    @Mock
    private ParameterResolver parameterResolver;

    @Mock
    private ProcessedCasesRecordHolder processedCasesRecordHolder;

    @Mock
    private CaseDeletionLoggingService caseDeletionLoggingService;

    @Mock
    private Clock clock;

    @InjectMocks
    private ApplicationExecutor applicationExecutor;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(Clock.systemUTC().instant());
        when(clock.getZone()).thenReturn(Clock.systemUTC().getZone());
        when(parameterResolver.getCutOffTime()).thenReturn(CUT_OFF_TIME);
    }

    @Test
    void testFindDeletableCandidatesWhenNoDeletableCandidatesFound() {
        doReturn(emptyList()).when(caseFindingService).findCasesDueDeletion();

        applicationExecutor.execute(1);

        verify(caseFindingService).findCasesDueDeletion();
        verify(caseFamiliesFilter).getDeletableCasesOnly(emptyList());
        verify(processedCasesRecordHolder).setSimulatedCases(emptySet());
        verify(caseDeletionService, times(0)).deleteCaseData(any());
    }

    @Test
    void testShouldDeleteTheCasesFound() {
        when(parameterResolver.getRequestLimit()).thenReturn(10);

        when(caseDeletionAsyncService.deleteCaseAsync(any()))
            .thenReturn(CompletableFuture.completedFuture(null));

        final List<CaseFamily> caseDataList = List.of(
            new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL, emptyList()),
            new CaseFamily(DELETABLE_CASE_DATA4_WITH_PAST_TTL, emptyList())
        );

        doReturn(caseDataList)
            .when(caseFindingService).findCasesDueDeletion();
        doReturn(caseDataList)
            .when(caseFamiliesFilter).getDeletableCasesOnly(caseDataList);

        applicationExecutor.execute(1);

        verify(caseFindingService).findCasesDueDeletion();
        verify(caseDeletionAsyncService, times(1)).deleteCaseAsync(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        verify(caseDeletionAsyncService, times(1)).deleteCaseAsync(DELETABLE_CASE_DATA4_WITH_PAST_TTL);
        verify(caseDeletionLoggingService, times(1)).logCases();
        verify(processedCasesRecordHolder, times(2)).addProcessedCase(any());
    }

    @Test
    void shouldLimitCaseDeletionToRequestsLimit() {
        // Given
        when(parameterResolver.getRequestLimit()).thenReturn(1);
        when(caseDeletionAsyncService.deleteCaseAsync(any()))
            .thenReturn(CompletableFuture.completedFuture(null));

        final List<CaseFamily> caseDataList = List.of(
            new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL, emptyList()),
            new CaseFamily(DELETABLE_CASE_DATA4_WITH_PAST_TTL, emptyList()),
            new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL, emptyList())
        );

        doReturn(caseDataList)
            .when(caseFindingService).findCasesDueDeletion();
        doReturn(caseDataList)
            .when(caseFamiliesFilter).getDeletableCasesOnly(caseDataList);

        applicationExecutor.execute(1);

        verify(caseFindingService).findCasesDueDeletion();
        verify(caseDeletionAsyncService, times(1)).deleteCaseAsync(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        verify(caseDeletionLoggingService, times(1)).logCases();
        verify(processedCasesRecordHolder, times(1)).addProcessedCase(any());
    }

    @ParameterizedTest
    @CsvSource({
        "2021-07-01T23:10:00Z, 2021-07-02T07:00:00Z, 1",
        "2021-07-01T01:10:00Z, 2021-07-02T07:00:00Z, 1"
    })
    void shouldNotRunAfterCutoffTime(String firstNow, String secondNow, int expectedDeleteCount) {
        when(parameterResolver.getRequestLimit()).thenReturn(10);
        when(caseDeletionAsyncService.deleteCaseAsync(any()))
            .thenReturn(CompletableFuture.completedFuture(null));
        final List<CaseFamily> caseDataList = List.of(
            new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL, emptyList()),
            new CaseFamily(DELETABLE_CASE_DATA4_WITH_PAST_TTL, emptyList()),
            new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL, emptyList())
        );

        doReturn(caseDataList)
            .when(caseFindingService).findCasesDueDeletion();
        doReturn(caseDataList)
            .when(caseFamiliesFilter).getDeletableCasesOnly(caseDataList);

        when(clock.instant())
            .thenReturn(Instant.parse("2021-07-01T22:00:00Z")) // app started
            .thenReturn(Instant.parse(firstNow)) // first "now" in loop
            .thenReturn(Instant.parse(secondNow)); // second "now" in loop

        applicationExecutor.execute(1);

        verify(caseFindingService).findCasesDueDeletion();
        verify(caseDeletionAsyncService, times(expectedDeleteCount)).deleteCaseAsync(DELETABLE_CASE_DATA_WITH_PAST_TTL);
    }

    @Test
    void shouldContinueDeletionsAfterLogAndAuditError(CapturedOutput output) {
        when(parameterResolver.getRequestLimit()).thenReturn(10);

        // Simulate async exception for the first case, success for the second
        when(caseDeletionAsyncService.deleteCaseAsync(DELETABLE_CASE_DATA_WITH_PAST_TTL))
            .thenReturn(CompletableFuture.failedFuture(new LogAndAuditException("Log and Audit error")));
        when(caseDeletionAsyncService.deleteCaseAsync(DELETABLE_CASE_DATA4_WITH_PAST_TTL))
            .thenReturn(CompletableFuture.completedFuture(null));

        final List<CaseFamily> caseDataList = List.of(
            new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL, emptyList()),
            new CaseFamily(DELETABLE_CASE_DATA4_WITH_PAST_TTL, emptyList())
        );

        doReturn(caseDataList).when(caseFindingService).findCasesDueDeletion();
        doReturn(caseDataList).when(caseFamiliesFilter).getDeletableCasesOnly(caseDataList);

        applicationExecutor.execute(1);

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(output).asString().contains("LogAndAudit error deleting case")
        );
        verify(caseFindingService).findCasesDueDeletion();
        verify(caseDeletionAsyncService, times(1)).deleteCaseAsync(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        verify(caseDeletionAsyncService, times(1)).deleteCaseAsync(DELETABLE_CASE_DATA4_WITH_PAST_TTL);
        verify(caseDeletionLoggingService, times(1)).logCases();
        verify(processedCasesRecordHolder, times(1)).addProcessedCase(any());
    }

    @Test
    void shouldLogAndRethrowOnUnexpectedAsyncError() {
        when(parameterResolver.getRequestLimit()).thenReturn(1);
        CaseData caseData = DELETABLE_CASE_DATA_WITH_PAST_TTL;
        List<CaseFamily> caseDataList = List.of(new CaseFamily(caseData, List.of()));

        doReturn(caseDataList).when(caseFindingService).findCasesDueDeletion();
        doReturn(caseDataList).when(caseFamiliesFilter).getDeletableCasesOnly(caseDataList);

        RuntimeException unexpected = new RuntimeException("Unexpected error");
        when(caseDeletionAsyncService.deleteCaseAsync(caseData))
            .thenReturn(CompletableFuture.failedFuture(unexpected));

        assertThrows(
            CompletionException.class,
            () -> applicationExecutor.execute(1)
        );

        verify(caseDeletionAsyncService).deleteCaseAsync(caseData);
        verify(processedCasesRecordHolder, times(0)).addProcessedCase(any());
        verify(processedCasesRecordHolder, times(0)).addFailedToDeleteCaseRef(any());
    }

}
