package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
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

    @InjectMocks
    private ApplicationExecutor applicationExecutor;

    @BeforeEach
    @SuppressWarnings("java:S8692")
    void setUp() {
        when(clock.instant()).thenReturn(Clock.systemUTC().instant());
        when(clock.getZone()).thenReturn(Clock.systemUTC().getZone());
        when(parameterResolver.getCutOffTime()).thenReturn(CUT_OFF_TIME);
        when(parameterResolver.getRequestLimit()).thenReturn(10);
        when(parameterResolver.getDeletableCaseTypes()).thenReturn(List.of(DELETABLE_CASE_TYPE));
        when(parameterResolver.getDeletableCaseTypesSimulation()).thenReturn(List.of(DELETABLE_CASE_TYPE_SIMULATION));
        when(parameterResolver.getElasticsearchHosts()).thenReturn(List.of());
        when(parameterResolver.getHearingCaseType()).thenReturn("hearing-case-type");
        when(caseCollectorService.getDeletableCases(List.of(DELETABLE_CASE_TYPE))).thenReturn(Set.of());
        when(caseCollectorService.getDeletableCases(List.of(DELETABLE_CASE_TYPE_SIMULATION))).thenReturn(Set.of());
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
        doThrow(new LogAndAuditException("log and audit failed"))
            .when(caseDeletionService).deleteCaseData(DELETABLE_CASE_DATA_WITH_PAST_TTL);

        applicationExecutor.execute();

        verify(caseDeletionService).deleteCaseData(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        verify(caseDeletionService).deleteCaseData(DELETABLE_CASE_DATA4_WITH_PAST_TTL);
        verify(processedCasesRecordHolder).addProcessedCase(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        verify(processedCasesRecordHolder).addProcessedCase(DELETABLE_CASE_DATA4_WITH_PAST_TTL);
        assertThat(output).contains("Error deleting case: 1 due to log and audit exception");
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
        verify(caseDeletionService, never()).deleteCaseData(DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1);
        verify(caseDeletionService, never()).deleteCaseData(DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_2);

        InOrder inOrder = inOrder(processedCasesRecordHolder, caseDeletionService, caseDeletionLoggingService);
        inOrder.verify(processedCasesRecordHolder).setSimulatedCases(simulatedCases);
        inOrder.verify(caseDeletionService).deleteCaseData(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        inOrder.verify(caseDeletionService).deleteCaseData(DELETABLE_CASE_DATA4_WITH_PAST_TTL);
        inOrder.verify(caseDeletionLoggingService).logCases();
    }
}
