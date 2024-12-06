package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionResolver;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionService;
import uk.gov.hmcts.reform.ccd.service.CaseFinderService;
import uk.gov.hmcts.reform.ccd.util.log.CaseFamiliesFilter;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA03_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA05_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA06_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA09_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA10_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA11_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA4_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.linkedCasesFamilyId_1;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.linkedCasesFamilyId_4;

@ExtendWith(MockitoExtension.class)
class ApplicationExecutorTest {
    @Mock
    private CaseFinderService caseFindingService;
    @Mock
    private CaseDeletionResolver caseDeletionResolver;

    @Mock
    private CaseFamiliesFilter caseFamiliesFilter;

    @Mock
    private CaseDeletionService caseDeletionService;

    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private ApplicationExecutor applicationExecutor;

    @Test
    void testFindDeletableCandidatesWhenNoDeletableCandidatesFound() {
        doReturn(emptyList()).when(caseFindingService).findCasesDueDeletion();

        applicationExecutor.execute();

        verify(caseFindingService).findCasesDueDeletion();
        verify(caseFamiliesFilter).getDeletableCasesOnly(emptyList());
        verify(caseDeletionResolver).logCaseDeletion(emptyList(),emptyList());
    }

    @Test
    void testShouldDeleteTheCasesFound() {
        when(parameterResolver.getRequestLimit()).thenReturn(2);

        final List<CaseFamily> caseDataList = List.of(
                new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL, emptyList()),
                new CaseFamily(DELETABLE_CASE_DATA4_WITH_PAST_TTL, emptyList())
        );

        doReturn(caseDataList)
                .when(caseFindingService).findCasesDueDeletion();
        doReturn(caseDataList)
                .when(caseFamiliesFilter).getDeletableCasesOnly(caseDataList);
        doNothing().when(caseDeletionResolver).logCaseDeletion(anyList(),anyList());

        applicationExecutor.execute();

        verify(caseFindingService).findCasesDueDeletion();
        verify(caseDeletionService, times(2)).deleteLinkedCaseFamilies(anyList());
        verify(caseDeletionResolver, times(1)).logCaseDeletion(anyList(),anyList());
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
        doNothing().when(caseDeletionResolver).logCaseDeletion(anyList(),anyList());

        applicationExecutor.execute();

        verify(caseFindingService).findCasesDueDeletion();
        verify(caseDeletionService, times(1)).deleteLinkedCaseFamilies(anyList());
        verify(caseDeletionResolver, times(1)).logCaseDeletion(anyList(),anyList());
    }

    @Test
    void shouldDeleteAllCaseDataIfRequestsLimitGreaterThanSize() {
        // Given
        when(parameterResolver.getRequestLimit()).thenReturn(3);

        final List<CaseFamily> caseDataList = List.of(
            new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL, emptyList()),
            new CaseFamily(DELETABLE_CASE_DATA4_WITH_PAST_TTL, emptyList()),
            new CaseFamily(DELETABLE_CASE_DATA03_WITH_PAST_TTL, emptyList())
        );

        doReturn(caseDataList)
            .when(caseFindingService).findCasesDueDeletion();
        doReturn(caseDataList)
            .when(caseFamiliesFilter).getDeletableCasesOnly(caseDataList);
        doNothing().when(caseDeletionResolver).logCaseDeletion(anyList(),anyList());

        applicationExecutor.execute();

        verify(caseFindingService).findCasesDueDeletion();
        verify(caseDeletionService, times(3)).deleteLinkedCaseFamilies(anyList());
        verify(caseDeletionResolver, times(1)).logCaseDeletion(anyList(),anyList());
    }


    @Test
    void shouldLimitCaseDeletionToRequestsLimitBasedOnLinkedCases() {
        // Given
        when(parameterResolver.getRequestLimit()).thenReturn(6);

        List<CaseData>  linkedCases = List.of(DELETABLE_CASE_DATA09_WITH_PAST_TTL,
                                              DELETABLE_CASE_DATA10_WITH_PAST_TTL);

        final List<CaseFamily> caseDataList = List.of(
            new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL, linkedCases),
            new CaseFamily(DELETABLE_CASE_DATA4_WITH_PAST_TTL, List.of(DELETABLE_CASE_DATA11_WITH_PAST_TTL))
        );
        doReturn(caseDataList)
            .when(caseFindingService).findCasesDueDeletion();
        doReturn(caseDataList)
            .when(caseFamiliesFilter).getDeletableCasesOnly(caseDataList);
        doNothing().when(caseDeletionResolver).logCaseDeletion(anyList(),anyList());

        applicationExecutor.execute();

        verify(caseFindingService).findCasesDueDeletion();
        verify(caseDeletionService, times(2)).deleteLinkedCaseFamilies(anyList());
        verify(caseDeletionResolver, times(1)).logCaseDeletion(anyList(),anyList());
    }

    @Test
    void shouldNotDeleteCaseDataIfRequestsLimitLessThanAllLinkedCases() {
        // Given
        when(parameterResolver.getRequestLimit()).thenReturn(5);

        // We will delete a two cases only caseFamily3 caseFamily4 and  because
        // the request limit is 5 and the linked cases are 6
        final CaseFamily caseFamily3 = new CaseFamily(DELETABLE_CASE_DATA05_WITH_PAST_TTL, emptyList());
        final CaseFamily caseFamily4 = new CaseFamily(DELETABLE_CASE_DATA06_WITH_PAST_TTL, emptyList());
        final List<CaseFamily> caseDataList = List.of(
            new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL, linkedCasesFamilyId_1),
            new CaseFamily(DELETABLE_CASE_DATA4_WITH_PAST_TTL, linkedCasesFamilyId_4),
            caseFamily3,
            caseFamily4
        );
        doReturn(caseDataList)
            .when(caseFindingService).findCasesDueDeletion();
        doReturn(caseDataList)
            .when(caseFamiliesFilter).getDeletableCasesOnly(caseDataList);
        doNothing().when(caseDeletionResolver).logCaseDeletion(anyList(),anyList());

        applicationExecutor.execute();

        verify(caseFindingService).findCasesDueDeletion();
        verify(caseDeletionService, times(1)).deleteLinkedCaseFamilies(List.of(caseFamily3));
        verify(caseDeletionService, times(1)).deleteLinkedCaseFamilies(List.of(caseFamily4));
        verify(caseDeletionResolver, times(1)).logCaseDeletion(anyList(),anyList());
    }

}
