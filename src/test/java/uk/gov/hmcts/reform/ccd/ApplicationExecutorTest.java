package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionService;
import uk.gov.hmcts.reform.ccd.service.CaseFindingService;

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
    private CaseFindingService caseFindingService;
    @Mock
    private CaseDeletionService caseDeletionService;

    @InjectMocks
    private ApplicationExecutor underTest;

    @Test
    void testFindDeletableCandidatesWhenNoDeletableCandidatesFound() {
        doReturn(emptyList()).when(caseFindingService).findDeletableCandidates();

        underTest.execute();

        verify(caseFindingService).findDeletableCandidates();
        verifyNoInteractions(caseDeletionService);
    }

    @Test
    void testShouldDeleteTheExpiredCasesFound() {
        doReturn(List.of(DELETABLE_CASE_WITH_PAST_TTL, DELETABLE_CASE_WITH_TODAY_TTL))
            .when(caseFindingService).findDeletableCandidates();
        doNothing().when(caseDeletionService).deleteCase(any(CaseDataEntity.class));

        underTest.execute();

        verify(caseFindingService).findDeletableCandidates();
        verify(caseDeletionService, times(2)).deleteCase(any(CaseDataEntity.class));
    }
}
