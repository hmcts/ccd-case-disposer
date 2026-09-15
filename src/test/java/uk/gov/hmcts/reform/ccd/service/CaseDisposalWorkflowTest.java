package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.CaseDeletionException;
import uk.gov.hmcts.reform.ccd.shell.config.ShellCaseProperties;
import uk.gov.hmcts.reform.ccd.shell.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.model.CcdCaseResponse;
import uk.gov.hmcts.reform.ccd.shell.model.ShellMappingResponse;
import uk.gov.hmcts.reform.ccd.shell.service.CcdCaseService;
import uk.gov.hmcts.reform.ccd.shell.service.ShellCaseDataMapper;
import uk.gov.hmcts.reform.ccd.shell.service.ShellDocumentService;
import uk.gov.hmcts.reform.ccd.shell.service.ShellMappingService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
class CaseDisposalWorkflowTest {

    private static final Long CASE_REFERENCE = 1234567890123456L;
    private static final String CASE_TYPE = "TestCaseType";
    private static final String CASE_STATE = "CaseOpen";

    @Mock
    private ShellMappingService shellMappingService;
    @Mock
    private CcdCaseService ccdCaseService;
    @Mock
    private CaseDeletionService caseDeletionService;
    @Mock
    private ShellCaseDataMapper shellCaseDataMapper;
    @Mock
    ShellDocumentService shellDocumentService;

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
            ccdCaseService,
            shellCaseProperties,
            shellCaseDataMapper,
            shellDocumentService
        );
    }

    @Test
    void shouldReturnDeletedWithoutLoadingShellCaseDataWhenShellCasesAreDisabled() {
        shellCaseProperties.setEnabled(false);

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.DELETED);
        verify(caseDeletionService, times(1)).deleteCaseData(caseData);
        verifyNoInteractions(shellMappingService, ccdCaseService, shellCaseDataMapper, shellDocumentService);
    }

    @Test
    void shouldReturnDeletedWithoutLoadingOriginalCaseWhenShellMappingDoesNotExist() {
        shellCaseProperties.setEnabled(true);
        when(shellMappingService.loadMappings(CASE_TYPE)).thenReturn(new ShellMappingResponse(null));

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.DELETED);
        verify(shellMappingService).loadMappings(CASE_TYPE);
        verifyNoInteractions(ccdCaseService, shellCaseDataMapper, shellDocumentService);
        verify(caseDeletionService, times(1)).deleteCaseData(caseData);
    }

    @Test
    void shouldCreateShellCaseWithDocumentHashesWhenShellMappingExists() {
        shellCaseProperties.setEnabled(true);
        ShellMappingResponse mapping = new ShellMappingResponse("ShellCaseType");
        ObjectNode originalData = JsonNodeFactory.instance.objectNode();
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode();
        CcdCaseResponse originalCase = new CcdCaseResponse(CASE_REFERENCE, CASE_TYPE, CASE_STATE, originalData);
        when(shellMappingService.loadMappings(CASE_TYPE)).thenReturn(mapping);
        when(ccdCaseService.loadCase(CASE_REFERENCE)).thenReturn(originalCase);
        when(ccdCaseService.findShellCase(mapping.getShellCaseTypeID(), CASE_REFERENCE))
            .thenReturn(Optional.empty());
        when(shellCaseDataMapper.map(originalData, mapping.getShellCaseMappings())).thenReturn(mappedData);

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.DELETED);
        verify(shellMappingService).loadMappings(CASE_TYPE);
        verify(ccdCaseService).loadCase(CASE_REFERENCE);
        verify(ccdCaseService).findShellCase(mapping.getShellCaseTypeID(), CASE_REFERENCE);
        verify(shellCaseDataMapper).map(originalData, mapping.getShellCaseMappings());
        verify(shellDocumentService).appendDocumentHashes(mappedData);
        verify(ccdCaseService).createShellCase(caseData, mappedData, mapping.getShellCaseTypeID());
        verify(caseDeletionService, times(1)).deleteCaseData(caseData);
    }

    @Test
    void shouldNotDeleteCaseWhenDocumentHashRetrievalFails() {
        shellCaseProperties.setEnabled(true);
        ShellMappingResponse mapping = new ShellMappingResponse("ShellCaseType");
        ObjectNode originalData = JsonNodeFactory.instance.objectNode();
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode();
        CcdCaseResponse originalCase = new CcdCaseResponse(CASE_REFERENCE, CASE_TYPE, CASE_STATE, originalData);
        when(shellMappingService.loadMappings(CASE_TYPE)).thenReturn(mapping);
        when(ccdCaseService.loadCase(CASE_REFERENCE)).thenReturn(originalCase);
        when(ccdCaseService.findShellCase(mapping.getShellCaseTypeID(), CASE_REFERENCE))
            .thenReturn(Optional.empty());
        when(shellCaseDataMapper.map(originalData, mapping.getShellCaseMappings())).thenReturn(mappedData);
        org.mockito.Mockito.doThrow(new ShellCaseException())
            .when(shellDocumentService).appendDocumentHashes(mappedData);

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.SHELL_FAILED);
        verifyNoInteractions(caseDeletionService);
    }

    @Test
    void shouldReturnShellFailedWhenLoadingShellMappingFails() {
        shellCaseProperties.setEnabled(true);
        when(shellMappingService.loadMappings(CASE_TYPE)).thenThrow(new ShellCaseException());

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.SHELL_FAILED);
        verify(shellMappingService).loadMappings(CASE_TYPE);
        verifyNoInteractions(ccdCaseService, shellCaseDataMapper, shellDocumentService, caseDeletionService);
    }

    @Test
    void shouldReturnShellFailedWhenLoadingOriginalCaseFails() {
        shellCaseProperties.setEnabled(true);
        when(shellMappingService.loadMappings(CASE_TYPE))
            .thenReturn(new ShellMappingResponse("ShellCaseType"));
        when(ccdCaseService.loadCase(CASE_REFERENCE)).thenThrow(new ShellCaseException());

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.SHELL_FAILED);
        verify(shellMappingService).loadMappings(CASE_TYPE);
        verify(ccdCaseService).loadCase(CASE_REFERENCE);
        verifyNoInteractions(shellCaseDataMapper, shellDocumentService, caseDeletionService);
    }

    @Test
    void shouldReturnDeletionFailedWhenCaseDeletionExceptionOccurs() {
        shellCaseProperties.setEnabled(true);
        when(shellMappingService.loadMappings(CASE_TYPE)).thenThrow(new CaseDeletionException());

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.DELETION_FAILED);
        verify(shellMappingService).loadMappings(CASE_TYPE);
        verifyNoInteractions(ccdCaseService, shellCaseDataMapper, shellDocumentService, caseDeletionService);
    }

    @Test
    void shouldNotCreateOrDeleteWhenShellCaseAlreadyExists() {
        shellCaseProperties.setEnabled(true);

        ShellMappingResponse mapping = new ShellMappingResponse("ShellCaseType");

        when(shellMappingService.loadMappings(CASE_TYPE)).thenReturn(mapping);
        when(ccdCaseService.findShellCase("ShellCaseType", CASE_REFERENCE))
            .thenReturn(Optional.of(9876543210123456L));

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.SHELL_ALREADY_EXISTS);

        verifyNoInteractions(shellCaseDataMapper, shellDocumentService, caseDeletionService);
    }
}
