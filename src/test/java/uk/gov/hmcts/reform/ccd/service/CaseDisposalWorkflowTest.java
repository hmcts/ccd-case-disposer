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
import uk.gov.hmcts.reform.ccd.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.config.ShellCaseProperties;
import uk.gov.hmcts.reform.ccd.shell.data.CcdCaseResponse;
import uk.gov.hmcts.reform.ccd.shell.model.ShellDocument;
import uk.gov.hmcts.reform.ccd.shell.model.ShellMappingResponse;
import uk.gov.hmcts.reform.ccd.shell.service.CaseDocumentResolver;
import uk.gov.hmcts.reform.ccd.shell.service.DocumentHashService;
import uk.gov.hmcts.reform.ccd.shell.service.OriginalCaseDataLoader;
import uk.gov.hmcts.reform.ccd.shell.service.ShellCaseDataMapper;
import uk.gov.hmcts.reform.ccd.shell.service.ShellDocumentHashAppender;
import uk.gov.hmcts.reform.ccd.shell.service.ShellMappingService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    @Mock
    private ShellCaseDataMapper shellCaseDataMapper;
    @Mock
    private CaseDocumentResolver caseDocumentResolver;
    @Mock
    private DocumentHashService documentHashService;
    @Mock
    private ShellDocumentHashAppender shellDocumentHashAppender;

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
            shellCaseProperties,
            shellCaseDataMapper,
            caseDocumentResolver,
            documentHashService,
            shellDocumentHashAppender
        );
    }

    @Test
    void shouldReturnDeletedWithoutLoadingShellCaseDataWhenShellCasesAreDisabled() {
        shellCaseProperties.setEnabled(false);

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.DELETED);
        verify(caseDeletionService, times(1)).deleteCaseData(caseData);
        verifyNoInteractions(shellMappingService, originalCaseDataLoader, shellCaseDataMapper,
            caseDocumentResolver, documentHashService, shellDocumentHashAppender);
    }

    @Test
    void shouldReturnDeletedWithoutLoadingOriginalCaseWhenShellMappingDoesNotExist() {
        shellCaseProperties.setEnabled(true);
        when(shellMappingService.loadMappings(CASE_TYPE)).thenReturn(new ShellMappingResponse(null));

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.DELETED);
        verify(shellMappingService).loadMappings(CASE_TYPE);
        verifyNoInteractions(originalCaseDataLoader, shellCaseDataMapper,
            caseDocumentResolver, documentHashService, shellDocumentHashAppender);
        verify(caseDeletionService, times(1)).deleteCaseData(caseData);
    }

    @Test
    void shouldRetrieveAndAppendDocumentHashesWhenShellMappingExists() {
        shellCaseProperties.setEnabled(true);
        ShellMappingResponse mapping = new ShellMappingResponse("ShellCaseType");
        ObjectNode originalData = JsonNodeFactory.instance.objectNode();
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode();
        UUID documentId = UUID.randomUUID();
        ShellDocument document = new ShellDocument(documentId, mappedData.putObject("document"));
        List<ShellDocument> documents = List.of(document);
        Map<UUID, String> hashes = Map.of(documentId, "hash-token");
        CcdCaseResponse originalCase = new CcdCaseResponse(CASE_REFERENCE, CASE_TYPE, CASE_STATE, originalData);
        when(shellMappingService.loadMappings(CASE_TYPE)).thenReturn(mapping);
        when(originalCaseDataLoader.load(CASE_REFERENCE)).thenReturn(originalCase);
        when(shellCaseDataMapper.map(originalData, mapping.getShellCaseMappings())).thenReturn(mappedData);
        when(caseDocumentResolver.resolveDocuments(mappedData)).thenReturn(documents);
        when(documentHashService.fetchHashes(documents)).thenReturn(hashes);

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.DELETED);
        verify(shellMappingService).loadMappings(CASE_TYPE);
        verify(originalCaseDataLoader).load(CASE_REFERENCE);
        verify(shellCaseDataMapper).map(originalData, mapping.getShellCaseMappings());
        verify(caseDocumentResolver).resolveDocuments(mappedData);
        verify(documentHashService).fetchHashes(documents);
        verify(shellDocumentHashAppender).appendHash(documents, hashes);
        verify(caseDeletionService, times(1)).deleteCaseData(caseData);
    }

    @Test
    void shouldNotDeleteCaseWhenDocumentHashRetrievalFails() {
        shellCaseProperties.setEnabled(true);
        ShellMappingResponse mapping = new ShellMappingResponse("ShellCaseType");
        ObjectNode originalData = JsonNodeFactory.instance.objectNode();
        ObjectNode mappedData = JsonNodeFactory.instance.objectNode();
        List<ShellDocument> documents = List.of();
        CcdCaseResponse originalCase = new CcdCaseResponse(CASE_REFERENCE, CASE_TYPE, CASE_STATE, originalData);
        when(shellMappingService.loadMappings(CASE_TYPE)).thenReturn(mapping);
        when(originalCaseDataLoader.load(CASE_REFERENCE)).thenReturn(originalCase);
        when(shellCaseDataMapper.map(originalData, mapping.getShellCaseMappings())).thenReturn(mappedData);
        when(caseDocumentResolver.resolveDocuments(mappedData)).thenReturn(documents);
        when(documentHashService.fetchHashes(documents)).thenThrow(new ShellCaseException());

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.SHELL_FAILED);
        verifyNoInteractions(shellDocumentHashAppender, caseDeletionService);
    }

    @Test
    void shouldReturnShellFailedWhenLoadingShellMappingFails() {
        shellCaseProperties.setEnabled(true);
        when(shellMappingService.loadMappings(CASE_TYPE)).thenThrow(new ShellCaseException());

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.SHELL_FAILED);
        verify(shellMappingService).loadMappings(CASE_TYPE);
        verifyNoInteractions(originalCaseDataLoader, shellCaseDataMapper, caseDocumentResolver,
            documentHashService, shellDocumentHashAppender, caseDeletionService);
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
        verifyNoInteractions(shellCaseDataMapper, caseDocumentResolver, documentHashService,
            shellDocumentHashAppender, caseDeletionService);
    }

    @Test
    void shouldReturnDeletionFailedWhenCaseDeletionExceptionOccurs() {
        shellCaseProperties.setEnabled(true);
        when(shellMappingService.loadMappings(CASE_TYPE)).thenThrow(new CaseDeletionException());

        CaseDisposalWorkflow.DisposalOutcome result = underTest.dispose(caseData);

        assertThat(result).isEqualTo(CaseDisposalWorkflow.DisposalOutcome.DELETION_FAILED);
        verify(shellMappingService).loadMappings(CASE_TYPE);
        verifyNoInteractions(originalCaseDataLoader, shellCaseDataMapper, caseDocumentResolver,
            documentHashService, shellDocumentHashAppender, caseDeletionService);
    }
}
