package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;
import uk.gov.hmcts.reform.ccd.util.SummaryStringLogBuilder;
import uk.gov.hmcts.reform.ccd.util.log.CaseDataViewBuilder;
import uk.gov.hmcts.reform.ccd.util.log.TableTextBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_2;

@ExtendWith(MockitoExtension.class)
class CaseDeletionLoggingServiceTest {

    @Spy
    private TableTextBuilder tableTextBuilder;

    @Spy
    private CaseDataViewBuilder caseDataViewBuilder;

    @Mock
    private ParameterResolver parameterResolver;

    @Spy
    private SummaryStringLogBuilder summaryStringLogBuilder;

    @Mock
    private ProcessedCasesRecordHolder processedCasesRecordHolder;

    @InjectMocks
    private CaseDeletionLoggingService caseDeletionLoggingService;

    @Test
    void shouldLogCaseFamilies() {

        when(processedCasesRecordHolder.getSuccessfullyDeletedCases())
            .thenReturn(List.of(DELETABLE_CASE_DATA_WITH_PAST_TTL));
        when(processedCasesRecordHolder.getSimulatedCases())
            .thenReturn(Set.of(DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1));
        when(processedCasesRecordHolder.getFailedToDeleteDeletedCases())
            .thenReturn(List.of(DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_2));

        when(parameterResolver.getAppInsightsLogSize()).thenReturn(10);
        caseDeletionLoggingService.logCases();

        verify(tableTextBuilder, times(1)).buildTextTable(anyList());
        verify(summaryStringLogBuilder, times(1))
                .buildSummaryString(anyList(), anyInt(), anyInt());
        verify(caseDataViewBuilder, times(3)).buildCaseDataViewList(anyList(), anyList(), anyString());
    }

    @Test
    void shouldLogWithNoDeletableOrSimulatedCasesFound() {

        when(processedCasesRecordHolder.getSuccessfullyDeletedCases())
            .thenReturn(Collections.emptyList());
        when(processedCasesRecordHolder.getSimulatedCases())
            .thenReturn(Collections.emptySet());
        when(processedCasesRecordHolder.getFailedToDeleteDeletedCases())
            .thenReturn(Collections.emptyList());

        when(parameterResolver.getAppInsightsLogSize()).thenReturn(10);

        caseDeletionLoggingService.logCases();

        verify(tableTextBuilder, times(0)).buildTextTable(anyList());
        verify(summaryStringLogBuilder, times(1))
                .buildSummaryString(0, 0, 0, 0, 0, 0,
                                    Collections.emptyMap());
        verify(caseDataViewBuilder, times(3)).buildCaseDataViewList(anyList(), anyList(), anyString());
    }
}
