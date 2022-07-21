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
import uk.gov.hmcts.reform.ccd.util.log.CaseDataViewHolder;
import uk.gov.hmcts.reform.ccd.util.log.TableTextBuilder;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_FAMILY_SIMULATION;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SUMMARY_HEADING_STRING;

@ExtendWith(MockitoExtension.class)
class CaseDeletionSimulationServiceTest {

    @Spy
    private TableTextBuilder tableTextBuilder;

    @Spy
    private CaseDataViewBuilder caseDataViewBuilder;

    @Spy
    private CaseDataViewHolder caseDataViewHolder;

    @Mock
    private ParameterResolver parameterResolver;

    @Mock
    private SummaryStringLogBuilder summaryStringLogBuilder;

    @InjectMocks
    private CaseDeletionSimulationService caseDeletionSimulationService;

    @Test
    void shouldLogCaseFamilies() {

        final List<CaseFamily> deletableCaseFamily = asList(DELETABLE_CASE_FAMILY);

        final List<CaseFamily> simulationCaseFamily = asList(DELETABLE_CASE_FAMILY_SIMULATION);

        when(parameterResolver.getAppInsightsLogSize()).thenReturn(7);
        when(summaryStringLogBuilder.buildSummaryString(anyList(),
                anyList(),
                anyInt(),
                anyInt()))
                .thenReturn(String.format(SUMMARY_HEADING_STRING, 1,
                        7));

        caseDeletionSimulationService.logCaseFamilies(deletableCaseFamily, simulationCaseFamily);

        verify(tableTextBuilder, times(1)).buildTextTable(anyList());
        verify(summaryStringLogBuilder, times(1))
                .buildSummaryString(anyList(), anyList(), anyInt(), anyInt());
        verify(caseDataViewHolder, times(1)).setUpData(anyList());
        verify(caseDataViewBuilder, times(2)).buildCaseDataViewList(anyList(), anyList(), anyBoolean());


        assertThat(caseDataViewHolder.getSimulatedCaseIds())
                .isNotNull()
                .containsExactlyInAnyOrder(30L, 31L);
    }
}