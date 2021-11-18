package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.RetentionStatus;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionService;
import uk.gov.hmcts.reform.ccd.service.CaseFinderService;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_WITH_TODAY_TTL;

@ExtendWith(MockitoExtension.class)
class ApplicationExecutorTest {
    @Mock
    private CaseFinderService caseFindingService;
    @Mock
    private CaseDeletionService caseDeletionService;

    @InjectMocks
    private ApplicationExecutor underTest;

    @Test
    void testFindDeletableCandidatesWhenNoDeletableCandidatesFound() {
        doReturn(emptyList()).when(caseFindingService).findCasesDueDeletion();

        underTest.execute();

        verify(caseFindingService).findCasesDueDeletion();
        verifyNoInteractions(caseDeletionService);
    }

    @Test
    void testShouldDeleteTheCasesFound() {
        final List<CaseData> caseDataList = List.of(
            new CaseData(
                DELETABLE_CASE_WITH_PAST_TTL.getId(),
                DELETABLE_CASE_WITH_PAST_TTL.getReference(),
                DELETABLE_CASE_WITH_PAST_TTL.getCaseType(),
                emptyList(),
                RetentionStatus.DELETE
            ),
            new CaseData(
                DELETABLE_CASE_WITH_TODAY_TTL.getId(),
                DELETABLE_CASE_WITH_TODAY_TTL.getReference(),
                DELETABLE_CASE_WITH_TODAY_TTL.getCaseType(),
                emptyList(),
                RetentionStatus.DELETE
            )
        );

        doReturn(caseDataList)
            .when(caseFindingService).findCasesDueDeletion();
        doNothing().when(caseDeletionService).deleteCase(any(CaseData.class));

        underTest.execute();

        verify(caseFindingService).findCasesDueDeletion();
        verify(caseDeletionService, times(2)).deleteCase(any(CaseData.class));
    }
}
