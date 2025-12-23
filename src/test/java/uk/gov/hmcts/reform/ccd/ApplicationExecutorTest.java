package uk.gov.hmcts.reform.ccd;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.exception.JobInterruptedException;
import uk.gov.hmcts.reform.ccd.exception.LogAndAuditException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionLoggingService;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionService;
import uk.gov.hmcts.reform.ccd.service.CaseFinderService;
import uk.gov.hmcts.reform.ccd.service.v2.CaseCollectorService;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;
import uk.gov.hmcts.reform.ccd.util.log.CaseFamiliesFilter;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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
    private ParameterResolver parameterResolver;

    @Mock
    private ProcessedCasesRecordHolder processedCasesRecordHolder;

    @Mock
    private CaseDeletionLoggingService caseDeletionLoggingService;

    @Mock
    private CaseCollectorService caseCollectorService;

    @Mock
    private Clock clock;

    @Mock
    ThreadPoolTaskExecutor taskExecutor;

    @InjectMocks
    private ApplicationExecutor applicationExecutor;

    @BeforeEach
    void setUp() {
        ThreadPoolTaskExecutor testExecutor = new ThreadPoolTaskExecutor();
        testExecutor.setCorePoolSize(1);
        testExecutor.setMaxPoolSize(1);
        testExecutor.setQueueCapacity(0);
        testExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        testExecutor.setThreadNamePrefix("test-executor-");
        testExecutor.initialize();
        applicationExecutor = new ApplicationExecutor(
            caseFindingService,
            caseDeletionService,
            caseFamiliesFilter,
            parameterResolver,
            processedCasesRecordHolder,
            caseDeletionLoggingService,
            caseCollectorService,
            testExecutor,
            clock
        );
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
        verify(caseDeletionService, times(1)).deleteCaseData(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        verify(caseDeletionService, times(1)).deleteCaseData(DELETABLE_CASE_DATA4_WITH_PAST_TTL);
        verify(caseDeletionLoggingService, times(1)).logCases();
        verify(processedCasesRecordHolder, times(2)).addProcessedCase(any());
    }

    @Test
    void shouldLimitCaseDeletionToRequestsLimit() {
        // Given
        when(parameterResolver.getRequestLimit()).thenReturn(1);

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
        verify(caseDeletionService, times(1)).deleteCaseData(DELETABLE_CASE_DATA_WITH_PAST_TTL);
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
        verify(caseDeletionService, times(expectedDeleteCount)).deleteCaseData(DELETABLE_CASE_DATA_WITH_PAST_TTL);
    }

    @Test
    void shouldContinueDeletionsAfterLogAndAuditError() {
        when(parameterResolver.getRequestLimit()).thenReturn(10);
        when(parameterResolver.getDeletableCaseTypes()).thenReturn(List.of("TEST"));

        Set<CaseData> caseDataSet = Set.of(
            DELETABLE_CASE_DATA_WITH_PAST_TTL,
            DELETABLE_CASE_DATA4_WITH_PAST_TTL
        );

        doReturn(caseDataSet)
            .when(caseCollectorService).getDeletableCases(List.of("TEST"));
        doThrow(new LogAndAuditException("Log and Audit error"))
            .when(caseDeletionService)
            .deleteCaseData(any(CaseData.class));

        applicationExecutor.execute(2);

        verify(caseCollectorService).getDeletableCases(List.of("TEST"));
        verify(caseDeletionService, times(2)).deleteCaseData(any());
        verify(caseDeletionLoggingService, times(1)).logCases();
        verify(processedCasesRecordHolder, times(2)).addProcessedCase(any());
    }

    @Test
    void shouldWaitForAllTasksToComplete() throws Exception {
        when(parameterResolver.getRequestLimit()).thenReturn(2);

        Set<CaseData> cases = Set.of(
            DELETABLE_CASE_DATA_WITH_PAST_TTL,
            DELETABLE_CASE_DATA4_WITH_PAST_TTL
        );

        doReturn(cases)
            .when(caseCollectorService)
            .getDeletableCases(any());

        CountDownLatch latch = new CountDownLatch(2);

        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(caseDeletionService).deleteCaseData(any());

        long start = System.currentTimeMillis();

        applicationExecutor.execute(2);

        long duration = System.currentTimeMillis() - start;

        assertThat(latch.await(0, TimeUnit.SECONDS)).isTrue();
        assertThat(duration).isGreaterThanOrEqualTo(0);

        verify(caseDeletionService, times(2)).deleteCaseData(any());
    }

    @Test
    void shouldCancelRunningTasksOnInterruption() {
        when(parameterResolver.getRequestLimit()).thenReturn(2);

        Set<CaseData> cases = Set.of(
            DELETABLE_CASE_DATA_WITH_PAST_TTL,
            DELETABLE_CASE_DATA4_WITH_PAST_TTL
        );

        doReturn(cases)
            .when(caseCollectorService)
            .getDeletableCases(any());

        doAnswer(invocation -> {
            Thread.sleep(5_000); // long task
            return null;
        }).when(caseDeletionService).deleteCaseData(any());

        Thread testThread = new Thread(() -> {
            assertThatThrownBy(() -> applicationExecutor.execute(2))
                .isInstanceOf(JobInterruptedException.class);
        });

        testThread.start();

        // allow tasks to start
        Awaitility.await()
            .atMost(1, TimeUnit.SECONDS)
            .untilAsserted(() ->
                               verify(caseDeletionService, atLeastOnce()).deleteCaseData(any())
            );

        testThread.interrupt();
    }
}
