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
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA4_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_DATA_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_ENTITY2_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.linkedCases;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.linkedCases2;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.linkedCases3;

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
        final CaseData caseData1 = new CaseData(
                DELETABLE_CASE_DATA_WITH_PAST_TTL.getId(),
                DELETABLE_CASE_DATA_WITH_PAST_TTL.getReference(),
                DELETABLE_CASE_DATA_WITH_PAST_TTL.getCaseType(),
                DELETABLE_CASE_DATA_WITH_PAST_TTL.getJurisdiction(),
                DELETABLE_CASE_DATA_WITH_PAST_TTL.getResolvedTtl(),
                DELETABLE_CASE_DATA_WITH_PAST_TTL.getId(),
                null
        );
        final CaseData caseData2 = new CaseData(
                DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getId(),
                DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getReference(),
                DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getCaseType(),
                DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getJurisdiction(),
                DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getResolvedTtl(),
                DELETABLE_CASE_ENTITY2_WITH_PAST_TTL.getId(),
                null
        );
        final List<CaseFamily> caseDataList = List.of(
                new CaseFamily(caseData1, emptyList()),
                new CaseFamily(caseData2, emptyList())
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
        final CaseData caseData1 = new CaseData(
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getId(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getReference(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getCaseType(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getJurisdiction(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getResolvedTtl(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getId(),
            null
        );
        final CaseData caseData2 = new CaseData(
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getId(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getReference(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getCaseType(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getJurisdiction(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getResolvedTtl(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getId(),
            null
        );
        final CaseData caseData3 = new CaseData(
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getId(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getReference(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getCaseType(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getJurisdiction(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getResolvedTtl(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getId(),
            null
        );
        final List<CaseFamily> caseDataList = List.of(
            new CaseFamily(caseData1, emptyList()),
            new CaseFamily(caseData2, emptyList()),
            new CaseFamily(caseData3, emptyList())
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
    void shouldLimitCaseDeletionToRequestsLimitBasedOnLinkedCases() {
        // Given
        when(parameterResolver.getRequestLimit()).thenReturn(3);
        final CaseData caseData1 = new CaseData(
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getId(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getReference(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getCaseType(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getJurisdiction(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getResolvedTtl(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getId(),
            null
        );
        final CaseData caseData2 = new CaseData(
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getId(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getReference(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getCaseType(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getJurisdiction(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getResolvedTtl(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getId(),
            null
        );
        final CaseData caseData3 = new CaseData(
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getId(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getReference(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getCaseType(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getJurisdiction(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getResolvedTtl(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getId(),
            null
        );

        final List<CaseFamily> caseDataList = List.of(
            new CaseFamily(caseData1, linkedCases),
            new CaseFamily(caseData2, emptyList()),
            new CaseFamily(caseData3, emptyList())
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
    void shouldDeleteCaseDataIfRequestsLimitGreaterThanAllLinkedCases() {
        // Given
        when(parameterResolver.getRequestLimit()).thenReturn(10);
        final CaseData caseData1 = new CaseData(
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getId(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getReference(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getCaseType(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getJurisdiction(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getResolvedTtl(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getId(),
            null
        );
        final CaseData caseData2 = new CaseData(
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getId(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getReference(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getCaseType(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getJurisdiction(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getResolvedTtl(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getId(),
            null
        );
        final CaseData caseData3 = new CaseData(
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getId(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getReference(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getCaseType(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getJurisdiction(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getResolvedTtl(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getId(),
            null
        );

        final List<CaseFamily> caseDataList = List.of(
            new CaseFamily(caseData1, linkedCases),
            new CaseFamily(caseData2, linkedCases2),
            new CaseFamily(caseData3, linkedCases3)
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
    void shouldDeleteAllCaseDataIfRequestsLimitGreaterThanSize() {
        // Given
        when(parameterResolver.getRequestLimit()).thenReturn(10);
        final CaseData caseData1 = new CaseData(
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getId(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getReference(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getCaseType(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getJurisdiction(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getResolvedTtl(),
            DELETABLE_CASE_DATA_WITH_PAST_TTL.getId(),
            null
        );
        final CaseData caseData2 = new CaseData(
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getId(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getReference(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getCaseType(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getJurisdiction(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getResolvedTtl(),
            DELETABLE_CASE_DATA4_WITH_PAST_TTL.getId(),
            null
        );
        final CaseData caseData3 = new CaseData(
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getId(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getReference(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getCaseType(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getJurisdiction(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getResolvedTtl(),
            DELETABLE_CASE_DATA03_WITH_PAST_TTL.getId(),
            null
        );
        final List<CaseFamily> caseDataList = List.of(
            new CaseFamily(caseData1, emptyList()),
            new CaseFamily(caseData2, emptyList()),
            new CaseFamily(caseData3, emptyList())
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
}
