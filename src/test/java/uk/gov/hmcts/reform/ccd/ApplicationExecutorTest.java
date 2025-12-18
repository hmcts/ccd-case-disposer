package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.JobInterruptedException;
import uk.gov.hmcts.reform.ccd.exception.LogAndAuditException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionLoggingService;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionService;
import uk.gov.hmcts.reform.ccd.service.v2.CaseCollectorService;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA4_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA5_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_2;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE_SIMULATION;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class ApplicationExecutorTest {

    private static final LocalTime CUT_OFF_TIME = LocalTime.parse("06:00");

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

    private ThreadPoolTaskExecutor testExecutor;

    private ApplicationExecutor applicationExecutor;

    @BeforeEach
    @SuppressWarnings("java:S8692")
    void setUp() {
        configureExecutor(1);
        when(clock.instant()).thenReturn(Clock.systemUTC().instant());
        when(clock.getZone()).thenReturn(Clock.systemUTC().getZone());
        when(parameterResolver.getCutOffTime()).thenReturn(CUT_OFF_TIME);
        when(parameterResolver.getRequestLimit()).thenReturn(10);
        when(parameterResolver.getDeletableCaseTypes()).thenReturn(List.of(DELETABLE_CASE_TYPE));
        when(parameterResolver.getDeletableCaseTypesSimulation()).thenReturn(List.of(DELETABLE_CASE_TYPE_SIMULATION));
        when(parameterResolver.getElasticsearchHosts()).thenReturn(List.of());
        when(parameterResolver.getHearingCaseType()).thenReturn("hearing-case-type");
    }

    @AfterEach
    void tearDown() {
        testExecutor.shutdown();
    }

    @Test
    void shouldLimitCaseDeletionToRequestsLimit() {
        Set<CaseData> deletableCases = new LinkedHashSet<>(List.of(
            DELETABLE_CASE_DATA_WITH_PAST_TTL,
            DELETABLE_CASE_DATA4_WITH_PAST_TTL,
            DELETABLE_CASE_DATA5_WITH_PAST_TTL
        ));
        when(parameterResolver.getRequestLimit()).thenReturn(2);
        when(caseCollectorService.getDeletableCases(List.of(DELETABLE_CASE_TYPE))).thenReturn(deletableCases);
        givenNoSimulatedCases();

        applicationExecutor.execute();

        verify(caseDeletionService).deleteCaseData(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        verify(caseDeletionService).deleteCaseData(DELETABLE_CASE_DATA4_WITH_PAST_TTL);
        verify(caseDeletionService, never()).deleteCaseData(DELETABLE_CASE_DATA5_WITH_PAST_TTL);
        verify(processedCasesRecordHolder).addProcessedCase(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        verify(processedCasesRecordHolder).addProcessedCase(DELETABLE_CASE_DATA4_WITH_PAST_TTL);
        verify(processedCasesRecordHolder, never()).addProcessedCase(DELETABLE_CASE_DATA5_WITH_PAST_TTL);
    }

    @ParameterizedTest
    @CsvSource({
        "2021-07-01T23:10:00Z, 2021-07-02T07:00:00Z, 0",
        "2021-07-01T01:10:00Z, 2021-07-02T07:00:00Z, 0"
    })
    void shouldNotRunAfterCutoffTime(String firstNow, String secondNow, int expectedDeleteCount) {
        when(clock.instant()).thenReturn(Instant.parse(firstNow), Instant.parse(secondNow));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(caseCollectorService.getDeletableCases(List.of(DELETABLE_CASE_TYPE)))
            .thenReturn(Set.of(DELETABLE_CASE_DATA_WITH_PAST_TTL));
        givenNoSimulatedCases();

        applicationExecutor.execute();

        verify(caseDeletionService, times(expectedDeleteCount)).deleteCaseData(any());
        verify(processedCasesRecordHolder, times(expectedDeleteCount)).addProcessedCase(any());
    }

    @Test
    void shouldContinueDeletionsAfterLogAndAuditError(CapturedOutput output) {
        Set<CaseData> deletableCases = new LinkedHashSet<>(List.of(
            DELETABLE_CASE_DATA_WITH_PAST_TTL,
            DELETABLE_CASE_DATA4_WITH_PAST_TTL
        ));
        when(caseCollectorService.getDeletableCases(List.of(DELETABLE_CASE_TYPE))).thenReturn(deletableCases);
        givenNoSimulatedCases();
        doAnswer(invocation -> {
            CaseData caseData = invocation.getArgument(0);
            if (DELETABLE_CASE_DATA_WITH_PAST_TTL.equals(caseData)) {
                throw new LogAndAuditException("log and audit failed");
            }
            return null;
        }).when(caseDeletionService).deleteCaseData(any());

        applicationExecutor.execute();

        verify(caseDeletionService).deleteCaseData(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        verify(caseDeletionService).deleteCaseData(DELETABLE_CASE_DATA4_WITH_PAST_TTL);
        verify(processedCasesRecordHolder).addProcessedCase(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        verify(processedCasesRecordHolder).addProcessedCase(DELETABLE_CASE_DATA4_WITH_PAST_TTL);
        assertThat(output).contains("Error deleting case 1");
    }

    @Test
    void shouldInvokeDeletionOnlyForDeletableCasesAndRecordSimulatedCases() {
        Set<CaseData> deletableCases = new LinkedHashSet<>(List.of(
            DELETABLE_CASE_DATA_WITH_PAST_TTL,
            DELETABLE_CASE_DATA4_WITH_PAST_TTL
        ));
        Set<CaseData> simulatedCases = Set.of(
            DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1,
            DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_2
        );
        when(caseCollectorService.getDeletableCases(List.of(DELETABLE_CASE_TYPE))).thenReturn(deletableCases);
        when(caseCollectorService.getDeletableCases(List.of(DELETABLE_CASE_TYPE_SIMULATION)))
            .thenReturn(simulatedCases);

        applicationExecutor.execute();

        verify(processedCasesRecordHolder).setSimulatedCases(simulatedCases);
        verify(caseDeletionService).deleteCaseData(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        verify(caseDeletionService).deleteCaseData(DELETABLE_CASE_DATA4_WITH_PAST_TTL);
        verify(caseDeletionService, never()).deleteCaseData(DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1);
        verify(caseDeletionService, never()).deleteCaseData(DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_2);
        verify(caseDeletionLoggingService).logCases();
    }

    @Test
    void shouldWaitForAllTasksToComplete() throws Exception {
        when(parameterResolver.getRequestLimit()).thenReturn(2);

        Set<CaseData> cases = Set.of(
            DELETABLE_CASE_DATA_WITH_PAST_TTL,
            DELETABLE_CASE_DATA4_WITH_PAST_TTL
        );

        when(caseCollectorService.getDeletableCases(List.of(DELETABLE_CASE_TYPE))).thenReturn(cases);
        givenNoSimulatedCases();

        CountDownLatch deletionCompleted = new CountDownLatch(2);

        doAnswer(invocation -> {
            deletionCompleted.countDown();
            return null;
        }).when(caseDeletionService).deleteCaseData(any());

        applicationExecutor.execute();

        assertThat(deletionCompleted.await(0, TimeUnit.SECONDS)).isTrue();

        verify(caseDeletionService, times(2)).deleteCaseData(any());
    }

    @Test
    void shouldRunDeletionsInParallelAndWaitForAllTasksBeforeLogging() throws Exception {
        configureExecutor(2);
        when(parameterResolver.getRequestLimit()).thenReturn(2);

        Set<CaseData> cases = new LinkedHashSet<>(List.of(
            DELETABLE_CASE_DATA_WITH_PAST_TTL,
            DELETABLE_CASE_DATA4_WITH_PAST_TTL
        ));

        when(caseCollectorService.getDeletableCases(List.of(DELETABLE_CASE_TYPE))).thenReturn(cases);
        givenNoSimulatedCases();

        CountDownLatch bothDeletionsStarted = new CountDownLatch(2);
        CountDownLatch releaseDeletions = new CountDownLatch(1);
        CountDownLatch bothDeletionsCompleted = new CountDownLatch(2);
        Set<String> deletionThreadNames = ConcurrentHashMap.newKeySet();

        doAnswer(invocation -> {
            deletionThreadNames.add(Thread.currentThread().getName());
            bothDeletionsStarted.countDown();
            if (!releaseDeletions.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting to release deletions");
            }
            bothDeletionsCompleted.countDown();
            return null;
        }).when(caseDeletionService).deleteCaseData(any());

        AtomicReference<Throwable> executionFailure = new AtomicReference<>();
        Thread executorThread = runExecutorAsync(executionFailure);

        assertThat(bothDeletionsStarted.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(executorThread.isAlive()).isTrue();
        assertThat(bothDeletionsCompleted.getCount()).isEqualTo(2);
        assertThat(deletionThreadNames)
            .hasSize(2)
            .allMatch(threadName -> threadName.startsWith("test-executor-"));
        verify(caseDeletionLoggingService, never()).logCases();

        releaseDeletions.countDown();
        executorThread.join(2_000);

        assertThat(executorThread.isAlive()).isFalse();
        assertThat(executionFailure.get()).isNull();
        assertThat(bothDeletionsCompleted.await(0, TimeUnit.SECONDS)).isTrue();
        verify(caseDeletionLoggingService).logCases();
        verify(processedCasesRecordHolder).addProcessedCase(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        verify(processedCasesRecordHolder).addProcessedCase(DELETABLE_CASE_DATA4_WITH_PAST_TTL);
    }

    @Test
    void shouldWaitForRemainingTasksAfterUnexpectedTaskFailure() throws Exception {
        configureExecutor(2);
        when(parameterResolver.getRequestLimit()).thenReturn(2);

        Set<CaseData> cases = new LinkedHashSet<>(List.of(
            DELETABLE_CASE_DATA_WITH_PAST_TTL,
            DELETABLE_CASE_DATA4_WITH_PAST_TTL
        ));

        when(caseCollectorService.getDeletableCases(List.of(DELETABLE_CASE_TYPE))).thenReturn(cases);
        givenNoSimulatedCases();

        CountDownLatch secondDeletionStarted = new CountDownLatch(1);
        CountDownLatch releaseSecondDeletion = new CountDownLatch(1);
        CountDownLatch secondDeletionCompleted = new CountDownLatch(1);

        doAnswer(invocation -> {
            CaseData caseData = invocation.getArgument(0);
            if (DELETABLE_CASE_DATA_WITH_PAST_TTL.equals(caseData)) {
                throw new IllegalStateException("unexpected deletion failure");
            }

            secondDeletionStarted.countDown();
            if (!releaseSecondDeletion.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting to release second deletion");
            }
            secondDeletionCompleted.countDown();
            return null;
        }).when(caseDeletionService).deleteCaseData(any());

        AtomicReference<Throwable> executionFailure = new AtomicReference<>();
        Thread executorThread = runExecutorAsync(executionFailure);

        assertThat(secondDeletionStarted.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(executorThread.isAlive()).isTrue();
        verify(caseDeletionLoggingService, never()).logCases();

        releaseSecondDeletion.countDown();
        executorThread.join(2_000);

        assertThat(executorThread.isAlive()).isFalse();
        assertThat(executionFailure.get()).isNull();
        assertThat(secondDeletionCompleted.await(0, TimeUnit.SECONDS)).isTrue();
        verify(caseDeletionService).deleteCaseData(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        verify(caseDeletionService).deleteCaseData(DELETABLE_CASE_DATA4_WITH_PAST_TTL);
        verify(processedCasesRecordHolder, never()).addProcessedCase(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        verify(processedCasesRecordHolder).addProcessedCase(DELETABLE_CASE_DATA4_WITH_PAST_TTL);
        verify(caseDeletionLoggingService).logCases();
    }

    @Test
    void shouldCancelRunningTasksOnInterruption() throws Exception {
        configureExecutor(2);
        when(parameterResolver.getRequestLimit()).thenReturn(2);

        Set<CaseData> cases = new LinkedHashSet<>(List.of(
            DELETABLE_CASE_DATA_WITH_PAST_TTL,
            DELETABLE_CASE_DATA4_WITH_PAST_TTL
        ));

        when(caseCollectorService.getDeletableCases(List.of(DELETABLE_CASE_TYPE))).thenReturn(cases);
        givenNoSimulatedCases();

        CountDownLatch bothDeletionsStarted = new CountDownLatch(2);
        CountDownLatch workerInterrupted = new CountDownLatch(1);
        CountDownLatch neverRelease = new CountDownLatch(1);

        doAnswer(invocation -> {
            bothDeletionsStarted.countDown();
            try {
                neverRelease.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                workerInterrupted.countDown();
                throw e;
            }
            return null;
        }).when(caseDeletionService).deleteCaseData(any());

        AtomicReference<Throwable> executionFailure = new AtomicReference<>();
        Thread testThread = runExecutorAsync(executionFailure);

        assertThat(bothDeletionsStarted.await(1, TimeUnit.SECONDS)).isTrue();
        verify(caseDeletionService, atLeastOnce()).deleteCaseData(any());

        testThread.interrupt();
        testThread.join(2_000);

        assertThat(testThread.isAlive()).isFalse();
        assertThat(executionFailure.get()).isInstanceOf(JobInterruptedException.class);
        assertThat(workerInterrupted.await(1, TimeUnit.SECONDS)).isTrue();
        verify(caseDeletionLoggingService, never()).logCases();
    }

    private Thread runExecutorAsync(AtomicReference<Throwable> executionFailure) {
        Thread thread = new Thread(() -> {
            try {
                applicationExecutor.execute();
            } catch (Throwable t) {
                executionFailure.set(t);
            }
        });
        thread.start();
        return thread;
    }

    private void configureExecutor(int poolSize) {
        if (testExecutor != null) {
            testExecutor.shutdown();
        }
        testExecutor = new ThreadPoolTaskExecutor();
        testExecutor.setCorePoolSize(poolSize);
        testExecutor.setMaxPoolSize(poolSize);
        testExecutor.setQueueCapacity(0);
        testExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        testExecutor.setThreadNamePrefix("test-executor-");
        testExecutor.initialize();
        applicationExecutor = new ApplicationExecutor(
            caseDeletionService,
            parameterResolver,
            processedCasesRecordHolder,
            caseDeletionLoggingService,
            caseCollectorService,
            testExecutor,
            clock
        );
    }

    private void givenNoSimulatedCases() {
        when(caseCollectorService.getDeletableCases(List.of(DELETABLE_CASE_TYPE_SIMULATION))).thenReturn(Set.of());
    }
}
