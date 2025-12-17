package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CaseDeletionAsyncServiceTest {

    private final CaseDeletionService caseDeletionService = mock(CaseDeletionService.class);
    private final CaseDeletionAsyncService asyncService = new CaseDeletionAsyncService(caseDeletionService);

    @Test
    void shouldCompleteSuccessfullyWhenNoException() {
        CaseData caseData = mock(CaseData.class);

        CompletableFuture<Void> future = asyncService.deleteCaseAsync(caseData);

        assertThat(future).isCompletedWithValue(null);
        verify(caseDeletionService, times(1)).deleteCaseData(caseData);
    }

    @Test
    void shouldCompleteExceptionallyWhenExceptionThrown() {
        CaseData caseData = mock(CaseData.class);
        RuntimeException ex = new RuntimeException("fail");
        doThrow(ex).when(caseDeletionService).deleteCaseData(caseData);

        CompletableFuture<Void> future = asyncService.deleteCaseAsync(caseData);

        assertThat(future).isCompletedExceptionally();
        Throwable thrown = catchThrowable(future::get);
        assertThat(thrown).isInstanceOf(ExecutionException.class)
            .hasCause(ex);
        verify(caseDeletionService, times(1)).deleteCaseData(caseData);
    }
}

