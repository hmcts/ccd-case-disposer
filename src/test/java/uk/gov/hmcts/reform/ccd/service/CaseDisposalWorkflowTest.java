package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.CaseDeletionException;
import uk.gov.hmcts.reform.ccd.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.config.ShellCaseProperties;
import uk.gov.hmcts.reform.ccd.shell.data.CcdCaseResponse;
import uk.gov.hmcts.reform.ccd.shell.model.ShellMappingResponse;
import uk.gov.hmcts.reform.ccd.shell.service.OriginalCaseDataLoader;
import uk.gov.hmcts.reform.ccd.shell.service.ShellMappingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseDisposalWorkflowTest {

    private static final Long CASE_REFERENCE = 1234567890123456L;
    private static final String CASE_TYPE = "TestCaseType";
    private static final String CASE_STATE = "CaseOpen";

    @Mock
    private ShellMappingService shellMappingService;
    @Mock
    private CaseDeletionService caseDeletionService;
    @Mock
    private OriginalCaseDataLoader originalCaseDataLoader;

    private ShellCaseProperties shellCaseProperties;
    private CaseDisposalWorkflow underTest;

    private final CaseData caseData = CaseData.builder()
        .id(1L)
        .reference(CASE_REFERENCE)
        .caseType(CASE_TYPE)
        .build();

    @BeforeEach
    void setUp() {
        shellCaseProperties = new ShellCaseProperties();
        underTest = new CaseDisposalWorkflow(
            shellMappingService,
            caseDeletionService,
            originalCaseDataLoader,
            shellCaseProperties
        );
    }

    @Test
    void shouldReturnDeletedWithoutLoadingShellCaseDataWhenShellCasesAreDisabled() {
        shellCaseProperties.setEnabled(false);

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.DELETED);
        verify(caseDeletionService, times(1)).deleteCaseData(caseData);
        verifyNoInteractions(shellMappingService, originalCaseDataLoader);
    }

    @Test
    void shouldReturnDeletedWithoutLoadingOriginalCaseWhenShellMappingDoesNotExist() {
        shellCaseProperties.setEnabled(true);
        when(shellMappingService.loadMappings(CASE_TYPE)).thenReturn(new ShellMappingResponse(null));

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.DELETED);
        verify(shellMappingService).loadMappings(CASE_TYPE);
        verifyNoInteractions(originalCaseDataLoader);
        verify(caseDeletionService, times(1)).deleteCaseData(caseData);
    }

    @Test
    void shouldLoadOriginalCaseAndReturnDeletedWhenShellMappingExists() {
        shellCaseProperties.setEnabled(true);
        ShellMappingResponse mapping = new ShellMappingResponse("ShellCaseType");
        CcdCaseResponse originalCase = new CcdCaseResponse(CASE_REFERENCE, CASE_TYPE, CASE_STATE, null);
        when(shellMappingService.loadMappings(CASE_TYPE)).thenReturn(mapping);
        when(originalCaseDataLoader.load(CASE_REFERENCE)).thenReturn(originalCase);

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.DELETED);
        verify(shellMappingService).loadMappings(CASE_TYPE);
        verify(originalCaseDataLoader).load(CASE_REFERENCE);
        verify(caseDeletionService, times(1)).deleteCaseData(caseData);
    }

    @Test
    void shouldReturnShellFailedWhenLoadingShellMappingFails() {
        shellCaseProperties.setEnabled(true);
        when(shellMappingService.loadMappings(CASE_TYPE)).thenThrow(new ShellCaseException());

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.SHELL_FAILED);
        verify(shellMappingService).loadMappings(CASE_TYPE);
        verifyNoInteractions(originalCaseDataLoader, caseDeletionService);
    }

    @Test
    void shouldReturnShellFailedWhenLoadingOriginalCaseFails() {
        shellCaseProperties.setEnabled(true);
        when(shellMappingService.loadMappings(CASE_TYPE))
            .thenReturn(new ShellMappingResponse("ShellCaseType"));
        when(originalCaseDataLoader.load(CASE_REFERENCE)).thenThrow(new ShellCaseException());

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.SHELL_FAILED);
        verify(shellMappingService).loadMappings(CASE_TYPE);
        verify(originalCaseDataLoader).load(CASE_REFERENCE);
        verifyNoInteractions(caseDeletionService);
    }

    @Test
    void shouldReturnDeletionFailedWhenCaseDeletionExceptionOccurs() {
        shellCaseProperties.setEnabled(true);
        when(shellMappingService.loadMappings(CASE_TYPE)).thenThrow(new CaseDeletionException());

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.DELETION_FAILED);
        verify(shellMappingService).loadMappings(CASE_TYPE);
        verifyNoInteractions(originalCaseDataLoader, caseDeletionService);
    }
}
