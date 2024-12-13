package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionLoggingService;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionService;
import uk.gov.hmcts.reform.ccd.service.CaseFinderService;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;
import uk.gov.hmcts.reform.ccd.util.log.CaseFamiliesFilter;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA4_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL;

@ExtendWith(MockitoExtension.class)
class ApplicationExecutorTest {
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

    @InjectMocks
    private ApplicationExecutor applicationExecutor;

    @Test
    void testFindDeletableCandidatesWhenNoDeletableCandidatesFound() {
        doReturn(emptyList()).when(caseFindingService).findCasesDueDeletion();

        applicationExecutor.execute();

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

        applicationExecutor.execute();

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

        applicationExecutor.execute();

        verify(caseFindingService).findCasesDueDeletion();
        verify(caseDeletionService, times(1)).deleteCaseData(DELETABLE_CASE_DATA_WITH_PAST_TTL);
        verify(caseDeletionLoggingService, times(1)).logCases();
        verify(processedCasesRecordHolder, times(1)).addProcessedCase(any());

    }
}
