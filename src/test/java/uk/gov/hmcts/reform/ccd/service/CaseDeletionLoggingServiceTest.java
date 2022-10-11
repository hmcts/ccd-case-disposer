package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.SummaryStringLogBuilder;
import uk.gov.hmcts.reform.ccd.util.log.CaseDataViewBuilder;
import uk.gov.hmcts.reform.ccd.util.log.SimulatedCaseDataViewHolder;
import uk.gov.hmcts.reform.ccd.util.log.TableTextBuilder;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY_SIMULATION;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.FAILED_CASE_FAMILY;

@ExtendWith(MockitoExtension.class)
class CaseDeletionLoggingServiceTest {

    @Spy
    private TableTextBuilder tableTextBuilder;

    @Spy
    private CaseDataViewBuilder caseDataViewBuilder;

    @Spy
    private SimulatedCaseDataViewHolder simulatedCaseDataViewHolder;

    @Mock
    private ParameterResolver parameterResolver;

    @Spy
    private SummaryStringLogBuilder summaryStringLogBuilder;

    @InjectMocks
    private CaseDeletionLoggingService caseDeletionLoggingService;

    @Test
    void shouldLogCaseFamilies() {

        final List<CaseFamily> deletableCaseFamily = asList(DELETABLE_CASE_FAMILY);
        final List<CaseFamily> simulationCaseFamily = asList(DELETABLE_CASE_FAMILY_SIMULATION);
        final List<CaseFamily> failedCaseFamily = asList(FAILED_CASE_FAMILY);

        when(parameterResolver.getAppInsightsLogSize()).thenReturn(10);
        caseDeletionLoggingService.logCaseFamilies(deletableCaseFamily, simulationCaseFamily, failedCaseFamily);

        verify(tableTextBuilder, times(1)).buildTextTable(anyList());
        verify(summaryStringLogBuilder, times(1))
                .buildSummaryString(anyList(), anyInt(), anyInt());
        verify(simulatedCaseDataViewHolder, times(1)).setUpData(anyList());
        verify(caseDataViewBuilder, times(3)).buildCaseDataViewList(anyList(), anyList(), anyString());


        assertThat(simulatedCaseDataViewHolder.getSimulatedCaseIds())
                .isNotNull()
                .containsExactlyInAnyOrder(30L, 31L);
    }

    @Test
    void shouldLogWithNoDeletableOrSimulatedCasesFound() {

        final List<CaseFamily> deletableCaseFamily = Collections.emptyList();

        final List<CaseFamily> simulationCaseFamily = Collections.emptyList();

        final List<CaseFamily> failedCaseFamily = Collections.emptyList();

        when(parameterResolver.getAppInsightsLogSize()).thenReturn(7);

        caseDeletionLoggingService.logCaseFamilies(deletableCaseFamily, simulationCaseFamily, failedCaseFamily);

        verify(tableTextBuilder, times(0)).buildTextTable(anyList());
        verify(summaryStringLogBuilder, times(1))
                .buildSummaryString(0, 0, 0, 0, 0, 0);
        verify(simulatedCaseDataViewHolder, times(1)).setUpData(Collections.emptyList());
        verify(caseDataViewBuilder, times(3)).buildCaseDataViewList(anyList(), anyList(), anyString());

        assertThat(simulatedCaseDataViewHolder.getSimulatedCaseIds()).isEmpty();
    }
}