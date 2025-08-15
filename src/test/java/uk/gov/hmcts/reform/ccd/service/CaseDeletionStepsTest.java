package uk.gov.hmcts.reform.ccd.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.hibernate.TransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.data.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.DocumentDeletionException;
import uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException;
import uk.gov.hmcts.reform.ccd.exception.HearingDeletionException;
import uk.gov.hmcts.reform.ccd.exception.LogAndAuditException;
import uk.gov.hmcts.reform.ccd.exception.RoleAssignmentDeletionException;
import uk.gov.hmcts.reform.ccd.fixture.CaseLinkEntityBuilder;
import uk.gov.hmcts.reform.ccd.service.remote.LogAndAuditRemoteOperation;
import uk.gov.hmcts.reform.ccd.service.remote.RemoteDisposeService;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_ENTITY_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.JURISDICTION;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.YESTERDAY;

class CaseDeletionStepsTest {

    @Mock
    private EntityManager em;

    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private CaseEventRepository caseEventRepository;

    @Mock
    private CaseLinkRepository caseLinkRepository;

    @Mock
    private RemoteDisposeService remoteDisposeService;

    @Mock
    private ProcessedCasesRecordHolder processedCasesRecordHolder;

    @Mock
    private LogAndAuditRemoteOperation logAndAuditRemoteOperation;

    @InjectMocks
    private CaseDeletionSteps caseDeletionSteps;

    private final CaseData caseData = new CaseData(1L, 1L, DELETABLE_CASE_TYPE, JURISDICTION, YESTERDAY, 1L, null);

    private final CaseLinkEntity caseLinkEntity1 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L).build();
    private final CaseLinkEntity caseLinkEntity2 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 11L).build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldDeleteCaseLinksSuccessfully() {
        CaseData caseData = mock(CaseData.class);
        final List<CaseLinkEntity> linkedCaseEntity = List.of(caseLinkEntity1, caseLinkEntity2);
        when(caseData.getId()).thenReturn(1L);
        when(caseLinkRepository.findByCaseIdOrLinkedCaseId(1L)).thenReturn(linkedCaseEntity);

        boolean result = caseDeletionSteps.deleteCaseLinks(caseData);
        assert result;
        verify(caseLinkRepository).deleteAll(anyList());
        verify(em).flush();
        verify(processedCasesRecordHolder, never()).addFailedToDeleteCaseRef(caseData);
    }

    @Test
    void shouldHandleExceptionWhileDeletingCaseLinks() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getId()).thenReturn(1L);
        when(caseLinkRepository.findByCaseIdOrLinkedCaseId(1L)).thenThrow(PersistenceException.class);

        assertThrows(PersistenceException.class, () -> caseDeletionSteps.deleteCaseLinks(caseData));

        verify(processedCasesRecordHolder, never()).addFailedToDeleteCaseRef(caseData);
    }

    @Test
    void shouldDeleteCaseSuccessfully() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getId()).thenReturn(1L);
        when(caseDataRepository.findById(1L)).thenReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL));

        caseDeletionSteps.deleteCase(caseData);

        verify(remoteDisposeService).remoteDeleteAll(caseData);
        verify(caseEventRepository).deleteByCaseDataId(1L);
        verify(caseDataRepository).delete(DELETABLE_CASE_ENTITY_WITH_PAST_TTL);
        verify(logAndAuditRemoteOperation).postCaseDeletionToLogAndAudit(caseData);
        verify(em).flush();
    }

    @Test
    void shouldHandleLogAndAuditExceptionWhileDeletingCase() {
        CaseData caseData = mock(CaseData.class);
        CaseDataEntity caseDataEntity = mock(CaseDataEntity.class);
        when(caseData.getId()).thenReturn(1L);
        when(caseDataRepository.findById(1L)).thenReturn(Optional.of(caseDataEntity));
        doThrow(LogAndAuditException.class).when(logAndAuditRemoteOperation).postCaseDeletionToLogAndAudit(caseData);

        assertThrows(LogAndAuditException.class, () -> caseDeletionSteps.deleteCase(caseData));

        verify(processedCasesRecordHolder).addFailedToDeleteCaseRef(caseData);
    }

    @Test
    void shouldHandlePersistenceExceptionWhileDeletingCase() {
        CaseData caseData = mock(CaseData.class);
        CaseDataEntity caseDataEntity = mock(CaseDataEntity.class);
        when(caseData.getId()).thenReturn(1L);
        when(caseDataRepository.findById(1L)).thenReturn(Optional.of(caseDataEntity));
        doThrow(PersistenceException.class).when(caseDataRepository).delete(caseDataEntity);

        assertThrows(PersistenceException.class, () -> caseDeletionSteps.deleteCase(caseData));

        verify(processedCasesRecordHolder).addFailedToDeleteCaseRef(caseData);
    }

    @Test
    void shouldHandleTransactionExceptionWhileDeletingCase() {
        CaseData caseData = mock(CaseData.class);
        CaseDataEntity caseDataEntity = mock(CaseDataEntity.class);
        when(caseData.getId()).thenReturn(1L);
        when(caseDataRepository.findById(1L)).thenReturn(Optional.of(caseDataEntity));
        doThrow(TransactionException.class).when(remoteDisposeService).remoteDeleteAll(caseData);

        assertThrows(TransactionException.class, () -> caseDeletionSteps.deleteCase(caseData));

        verify(processedCasesRecordHolder).addFailedToDeleteCaseRef(caseData);
    }

    @Test
    void shouldNotDeleteCasesIfDocumentExceptionOccur() {
        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);

        doThrow(DocumentDeletionException.class).when(remoteDisposeService).remoteDeleteAll(any(CaseData.class));

        // WHEN
        caseDeletionSteps.deleteCase(caseData);

        // THEN
        verify(remoteDisposeService, times(1)).remoteDeleteAll(any(CaseData.class));
        verify(caseEventRepository, times(0)).deleteByCaseDataId(anyLong());
        verify(caseDataRepository, times(0)).delete(any(CaseDataEntity.class));
        verify(processedCasesRecordHolder, times(1)).addFailedToDeleteCaseRef(caseData);
    }

    @Test
    void shouldNotDeleteCasesIfElasticSearchOperationExceptionOccur() {
        // GIVEN
        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);

        doThrow(ElasticsearchOperationException.class).when(remoteDisposeService).remoteDeleteAll(any(CaseData.class));

        // WHEN
        caseDeletionSteps.deleteCase(caseData);

        // THEN
        verify(remoteDisposeService, times(1)).remoteDeleteAll(any(CaseData.class));
        verify(caseEventRepository, times(0)).deleteByCaseDataId(anyLong());
        verify(caseDataRepository, times(0)).delete(any(CaseDataEntity.class));
        verify(processedCasesRecordHolder, times(1)).addFailedToDeleteCaseRef(caseData);
    }

    @Test
    void shouldNotDeleteCasesIfRoleAssignmentDeletionExceptionOccur() {
        // GIVEN
        //doReturn(List.of(caseLinkEntity1)).when(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);

        doThrow(RoleAssignmentDeletionException.class).when(remoteDisposeService).remoteDeleteAll(any(CaseData.class));

        // WHEN
        caseDeletionSteps.deleteCase(caseData);

        // THEN
        //verify(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        verify(remoteDisposeService, times(1)).remoteDeleteAll(any(CaseData.class));
        verify(caseEventRepository, times(0)).deleteByCaseDataId(anyLong());
        verify(caseDataRepository, times(0)).delete(any(CaseDataEntity.class));
        verify(processedCasesRecordHolder, times(1)).addFailedToDeleteCaseRef(caseData);
    }

    @Test
    void shouldNotDeleteCasesIfHearingDeletionExceptionOccur() {

        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);

        doThrow(HearingDeletionException.class).when(remoteDisposeService).remoteDeleteAll(any(CaseData.class));

        // WHEN
        caseDeletionSteps.deleteCase(caseData);

        // THEN
        verify(remoteDisposeService, times(1)).remoteDeleteAll(any(CaseData.class));
        verify(caseEventRepository, times(0)).deleteByCaseDataId(anyLong());
        verify(caseDataRepository, times(0)).delete(any(CaseDataEntity.class));
        verify(processedCasesRecordHolder, times(1)).addFailedToDeleteCaseRef(caseData);
    }

    @Test
    void shouldThrowExceptionOnLogAndAuditException() {
        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);

        doThrow(LogAndAuditException.class)
            .when(logAndAuditRemoteOperation)
            .postCaseDeletionToLogAndAudit(any(CaseData.class));

        // WHEN
        Throwable thrown = catchThrowable(() -> caseDeletionSteps.deleteCase(caseData));

        // THEN
        //verify(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        verify(remoteDisposeService, times(1)).remoteDeleteAll(any(CaseData.class));
        verify(caseEventRepository, times(1)).deleteByCaseDataId(anyLong());
        verify(caseDataRepository, times(1)).delete(any(CaseDataEntity.class));
        verify(processedCasesRecordHolder, times(1)).addFailedToDeleteCaseRef(caseData);
        verify(logAndAuditRemoteOperation, times(1)).postCaseDeletionToLogAndAudit(any());
        assertThat(thrown).isInstanceOf(LogAndAuditException.class);

    }
}


