package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionService;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.CASE_DATA_TODAY;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.CASE_DATA_YESTERDAY;

@ExtendWith(MockitoExtension.class)
class ApplicationExecutorTest {
    @Mock
    private CaseDeletionService caseDeletionService;

    @InjectMocks
    private ApplicationExecutor underTest;

    @Test
    void testWhenNoExpiredCasesFound() {
        doReturn(emptyList()).when(caseDeletionService).getExpiredCases();

        underTest.execute();

        verify(caseDeletionService).getExpiredCases();
        verifyNoMoreInteractions(caseDeletionService);
    }

    @Test
    void testShouldDeleteTheExpiredCasesFound() {
        doReturn(List.of(CASE_DATA_YESTERDAY, CASE_DATA_TODAY)).when(caseDeletionService).getExpiredCases();
        doNothing().when(caseDeletionService).deleteCase(any(CaseDataEntity.class));

        underTest.execute();

        verify(caseDeletionService).getExpiredCases();
        verify(caseDeletionService, times(2)).deleteCase(any(CaseDataEntity.class));
    }
}
