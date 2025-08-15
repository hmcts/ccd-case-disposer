package uk.gov.hmcts.reform.ccd.service;

import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseDeletionServiceTest {

    @Mock
    private CaseDeletionSteps steps;

    @Mock
    private ProcessedCasesRecordHolder processedCasesRecordHolder;

    @InjectMocks
    private CaseDeletionService caseDeletionService;

    private CaseData caseData;

    private CaseData buildCaseData() {
        return new CaseData(1L, 1504259909035001L, "caseType", "BEFTA_MASTER", null, 1L, null);
    }

    @Test
    void shouldDeleteCaseDataSuccessfully() {
        CaseData cd = buildCaseData();
        when(steps.deleteCaseLinks(cd)).thenReturn(true);

        assertDoesNotThrow(() -> caseDeletionService.deleteCaseData(cd));

        verify(steps, times(1)).deleteCaseLinks(cd);
        verify(steps, times(1)).deleteCase(cd);
        verifyNoMoreInteractions(steps);
        verifyNoInteractions(processedCasesRecordHolder);
    }

    @Test
    void shouldSkipCaseDeletionWhenLinksNotDeleted() {
        CaseData cd = buildCaseData();
        when(steps.deleteCaseLinks(cd)).thenReturn(false);

        assertDoesNotThrow(() -> caseDeletionService.deleteCaseData(cd));

        verify(steps, times(1)).deleteCaseLinks(cd);
        verify(steps, never()).deleteCase(any());
        verifyNoMoreInteractions(steps);
        verifyNoInteractions(processedCasesRecordHolder);
    }

    @Test
    void shouldCatchExceptionFromLinksStepAndNotCallCaseDeletion() {
        CaseData cd = buildCaseData();
        doThrow(new DataIntegrityViolationException("FK fail"))
                .when(steps).deleteCaseLinks(cd);

        assertDoesNotThrow(() -> caseDeletionService.deleteCaseData(cd));

        verify(steps, times(1)).deleteCaseLinks(cd);
        verify(steps, never()).deleteCase(any());
        verifyNoMoreInteractions(steps);
        verifyNoInteractions(processedCasesRecordHolder);
    }

    @Test
    void shouldCatchTransactionExceptionFromLinksStepAndNotCallCaseDeletion() {
        CaseData cd = buildCaseData();
        doThrow(new TransactionSystemException("tx problem"))
                .when(steps).deleteCaseLinks(cd);

        assertDoesNotThrow(() -> caseDeletionService.deleteCaseData(cd));

        verify(steps, times(1)).deleteCaseLinks(cd);
        verify(steps, never()).deleteCase(any());
        verifyNoMoreInteractions(steps);
        verifyNoInteractions(processedCasesRecordHolder);
    }

    @Test
    void shouldCatchPersistenceExceptionFromCaseStep() {
        CaseData cd = buildCaseData();
        when(steps.deleteCaseLinks(cd)).thenReturn(true);
        doThrow(new PersistenceException("flush fail"))
                .when(steps).deleteCase(cd);

        assertDoesNotThrow(() -> caseDeletionService.deleteCaseData(cd));

        verify(steps, times(1)).deleteCaseLinks(cd);
        verify(steps, times(1)).deleteCase(cd);
        verifyNoMoreInteractions(steps);
        verifyNoInteractions(processedCasesRecordHolder);
    }
}
