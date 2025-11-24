package uk.gov.hmcts.reform.ccd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.CaseEventSignificantItemsRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_ENTITY_WITH_PAST_TTL;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.JURISDICTION;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.YESTERDAY;

@ExtendWith(MockitoExtension.class)
class CaseDeletionServiceTest {
    @Mock
    private CaseDataRepository caseDataRepository;
    @Mock
    private CaseEventRepository caseEventRepository;
    @Mock
    private CaseLinkRepository caseLinkRepository;
    @Mock
    private CaseEventSignificantItemsRepository caseEventSignificantItemsRepository;
    @Mock
    private RemoteDisposeService remoteDisposeService;
    @Mock
    private LogAndAuditRemoteOperation logAndAuditRemoteOperation;
    @Mock
    private ProcessedCasesRecordHolder processedCasesRecordHolder;

    @InjectMocks
    private CaseDeletionService underTest;

    private final CaseData caseData = new CaseData(1L, 1L, DELETABLE_CASE_TYPE, JURISDICTION, YESTERDAY, 1L, null);

    private final CaseLinkEntity caseLinkEntity1 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 10L).build();
    private final CaseLinkEntity caseLinkEntity2 = new CaseLinkEntityBuilder(1L, DELETABLE_CASE_TYPE, 11L).build();

    @Test
    void shouldDeleteCase() {
        doReturn(0).when(caseEventSignificantItemsRepository).deleteByCaseDataId(anyLong());
        doReturn(0).when(caseEventRepository).deleteByCaseDataId(anyLong());
        doNothing().when(caseDataRepository).delete(any(CaseDataEntity.class));
        doNothing().when(remoteDisposeService).remoteDeleteAll(caseData);

        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);

        underTest.deleteCaseData(caseData);

        verify(caseLinkRepository, never()).deleteAll(anyList());
        verify(caseEventRepository).deleteByCaseDataId(anyLong());
        verify(caseDataRepository).findById(1L);
        verify(caseDataRepository).delete(DELETABLE_CASE_ENTITY_WITH_PAST_TTL);
        verify(remoteDisposeService).remoteDeleteAll(caseData);
    }

    @Test
    void shouldDeleteCaseWithLinkedCases() {
        final List<CaseLinkEntity> linkedCaseEntity = List.of(caseLinkEntity1, caseLinkEntity2);

        doReturn(0).when(caseEventSignificantItemsRepository).deleteByCaseDataId(anyLong());
        doReturn(0).when(caseEventRepository).deleteByCaseDataId(anyLong());
        doNothing().when(caseDataRepository).delete(any(CaseDataEntity.class));
        doNothing().when(remoteDisposeService).remoteDeleteAll(caseData);

        doReturn(linkedCaseEntity).when(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);

        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);

        underTest.deleteCaseData(caseData);

        verify(caseLinkRepository, times(1)).deleteAll(linkedCaseEntity);
        verify(caseEventSignificantItemsRepository).deleteByCaseDataId(1L);
        verify(caseEventRepository).deleteByCaseDataId(1L);
        verify(caseDataRepository).findById(1L);
        verify(caseDataRepository).delete(DELETABLE_CASE_ENTITY_WITH_PAST_TTL);
        verify(remoteDisposeService).remoteDeleteAll(caseData);
        verify(logAndAuditRemoteOperation, times(1)).postCaseDeletionToLogAndAudit(caseData);
    }

    @Test
    void shouldLogErrorWhenDeleteFails() {
        // GIVEN
        doThrow(IllegalArgumentException.class).when(caseEventRepository).deleteByCaseDataId(anyLong());
        doReturn(Optional.of(mock(CaseDataEntity.class))).when(caseDataRepository).findById(anyLong());

        // WHEN
        catchThrowable(() -> underTest.deleteCase(caseData));

        // THEN
        verify(processedCasesRecordHolder).addFailedToDeleteCaseRef(caseData);
        verify(caseEventSignificantItemsRepository).deleteByCaseDataId(anyLong());
        verify(caseEventRepository).deleteByCaseDataId(anyLong());
        verify(remoteDisposeService).remoteDeleteAll(any(CaseData.class));

        verifyNoInteractions(caseLinkRepository);
    }


    @Test
    void shouldLogErrorWhenDeleteCaseLinksFails() {
        // GIVEN
        doThrow(IllegalArgumentException.class).when(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);

        // WHEN
        catchThrowable(() -> underTest.deleteCaseLinks(caseData));

        // THEN
        verify(caseLinkRepository, times(1)).findByCaseIdOrLinkedCaseId(1L);
        verify(processedCasesRecordHolder, times(1)).addFailedToDeleteCaseRef(caseData);
        verifyNoMoreInteractions(caseLinkRepository);
    }


    @Test
    void shouldNotDeleteCasesIfDocumentExceptionOccur() {
        // GIVEN
        doReturn(List.of(caseLinkEntity1)).when(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);

        doThrow(DocumentDeletionException.class).when(remoteDisposeService).remoteDeleteAll(any(CaseData.class));

        // WHEN
        underTest.deleteCaseData(caseData);

        // THEN
        verify(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        verify(remoteDisposeService, times(1)).remoteDeleteAll(any(CaseData.class));
        verify(caseEventSignificantItemsRepository, times(0)).deleteByCaseDataId(anyLong());
        verify(caseEventRepository, times(0)).deleteByCaseDataId(anyLong());
        verify(caseDataRepository, times(0)).delete(any(CaseDataEntity.class));
        verify(processedCasesRecordHolder, times(1)).addFailedToDeleteCaseRef(caseData);
    }

    @Test
    void shouldNotDeleteCasesIfElasticSearchOperationExceptionOccur() {
        // GIVEN
        doReturn(List.of(caseLinkEntity1)).when(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);

        doThrow(ElasticsearchOperationException.class).when(remoteDisposeService).remoteDeleteAll(any(CaseData.class));

        // WHEN
        underTest.deleteCaseData(caseData);

        // THEN
        verify(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        verify(remoteDisposeService, times(1)).remoteDeleteAll(any(CaseData.class));
        verify(caseEventSignificantItemsRepository, times(0)).deleteByCaseDataId(anyLong());
        verify(caseEventRepository, times(0)).deleteByCaseDataId(anyLong());
        verify(caseDataRepository, times(0)).delete(any(CaseDataEntity.class));
        verify(processedCasesRecordHolder, times(1)).addFailedToDeleteCaseRef(caseData);
    }

    @Test
    void shouldNotDeleteCasesIfRoleAssignmentDeletionExceptionOccur() {
        // GIVEN
        doReturn(List.of(caseLinkEntity1)).when(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);

        doThrow(RoleAssignmentDeletionException.class).when(remoteDisposeService).remoteDeleteAll(any(CaseData.class));

        // WHEN
        underTest.deleteCaseData(caseData);

        // THEN
        verify(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        verify(remoteDisposeService, times(1)).remoteDeleteAll(any(CaseData.class));
        verify(caseEventSignificantItemsRepository, times(0)).deleteByCaseDataId(anyLong());
        verify(caseEventRepository, times(0)).deleteByCaseDataId(anyLong());
        verify(caseDataRepository, times(0)).delete(any(CaseDataEntity.class));
        verify(processedCasesRecordHolder, times(1)).addFailedToDeleteCaseRef(caseData);
    }

    @Test
    void shouldNotDeleteCasesIfHearingDeletionExceptionOccur() {
        // GIVEN
        doReturn(List.of(caseLinkEntity1)).when(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);

        doThrow(HearingDeletionException.class).when(remoteDisposeService).remoteDeleteAll(any(CaseData.class));

        // WHEN
        underTest.deleteCaseData(caseData);

        // THEN
        verify(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        verify(remoteDisposeService, times(1)).remoteDeleteAll(any(CaseData.class));
        verify(caseEventSignificantItemsRepository, times(0)).deleteByCaseDataId(anyLong());
        verify(caseEventRepository, times(0)).deleteByCaseDataId(anyLong());
        verify(caseDataRepository, times(0)).delete(any(CaseDataEntity.class));
        verify(processedCasesRecordHolder, times(1)).addFailedToDeleteCaseRef(caseData);
    }

    @Test
    void shouldThrowExceptionOnLogAndAuditException() {
        doReturn(List.of(caseLinkEntity1)).when(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);

        doThrow(LogAndAuditException.class)
            .when(logAndAuditRemoteOperation)
            .postCaseDeletionToLogAndAudit(any(CaseData.class));

        // WHEN
        Throwable thrown = catchThrowable(() -> underTest.deleteCaseData(caseData));

        // THEN
        verify(caseLinkRepository).findByCaseIdOrLinkedCaseId(1L);
        verify(remoteDisposeService, times(1)).remoteDeleteAll(any(CaseData.class));
        verify(caseEventSignificantItemsRepository, times(1)).deleteByCaseDataId(anyLong());
        verify(caseEventRepository, times(1)).deleteByCaseDataId(anyLong());
        verify(caseDataRepository, times(1)).delete(any(CaseDataEntity.class));
        verify(processedCasesRecordHolder, times(1)).addFailedToDeleteCaseRef(caseData);
        verify(logAndAuditRemoteOperation, times(1)).postCaseDeletionToLogAndAudit(any());
        assertThat(thrown).isInstanceOf(LogAndAuditException.class);

    }

    @Test
    void shouldDeleteCaseWithSignificantItems() {
        doReturn(1).when(caseEventSignificantItemsRepository).deleteByCaseDataId(anyLong());
        doReturn(1).when(caseEventRepository).deleteByCaseDataId(anyLong());
        doNothing().when(caseDataRepository).delete(any(CaseDataEntity.class));
        doNothing().when(remoteDisposeService).remoteDeleteAll(caseData);


        doReturn(Optional.of(DELETABLE_CASE_ENTITY_WITH_PAST_TTL)).when(caseDataRepository).findById(1L);

        underTest.deleteCaseData(caseData);

        verify(caseLinkRepository, never()).deleteAll(anyList());
        verify(caseEventSignificantItemsRepository).deleteByCaseDataId(1L);
        verify(caseEventRepository).deleteByCaseDataId(1L);
        verify(caseDataRepository).findById(1L);
        verify(caseDataRepository).delete(DELETABLE_CASE_ENTITY_WITH_PAST_TTL);
        verify(remoteDisposeService).remoteDeleteAll(caseData);
    }

    @Test
    void shouldLogErrorWhenDeleteEventSignificantItemsFails() {
        // GIVEN
        doThrow(IllegalArgumentException.class).when(caseEventSignificantItemsRepository)
            .deleteByCaseDataId(anyLong());
        doReturn(Optional.of(mock(CaseDataEntity.class))).when(caseDataRepository).findById(anyLong());

        // WHEN
        catchThrowable(() -> underTest.deleteCase(caseData));

        // THEN
        verify(processedCasesRecordHolder).addFailedToDeleteCaseRef(caseData);
        verify(caseEventSignificantItemsRepository).deleteByCaseDataId(anyLong());
        verify(remoteDisposeService).remoteDeleteAll(any(CaseData.class));

        verifyNoInteractions(caseLinkRepository);
    }
}
