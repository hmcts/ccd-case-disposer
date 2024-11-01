package uk.gov.hmcts.reform.ccd.service.remote;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RemoteDisposeServiceTest {

    @Spy
    private List<DisposeRemoteOperation> disposeRemoteOperations = new ArrayList<>();

    @InjectMocks
    private RemoteDisposeService remoteDisposeService;


    @Test
    void shouldDeleteDocumentsWhenNotHearingCase() {
        final CaseData caseData = CaseData.builder().caseType("NonHearingCase").build();
        final DisposeDocumentsRemoteOperation disposeDocumentsRemoteOperation =
            mock(DisposeDocumentsRemoteOperation.class);

        disposeRemoteOperations.add(disposeDocumentsRemoteOperation);

        remoteDisposeService.remoteDeleteAll(caseData);

        verify(disposeDocumentsRemoteOperation, times(1)).delete(caseData);
    }

    @Test
    void shouldDeleteHearingsWhenHearingCase() {
        final CaseData caseData = CaseData.builder().caseType("HearingRecordings").build();
        final DisposeHearingsRemoteOperation disposeHearingsRemoteOperation =
            mock(DisposeHearingsRemoteOperation.class);

        disposeRemoteOperations.add(disposeHearingsRemoteOperation);

        remoteDisposeService.remoteDeleteAll(caseData);

        verify(disposeHearingsRemoteOperation, times(1)).delete(caseData);
    }

    @Test
    void shouldNotDeleteDocumentsWhenHearingCase() {
        final CaseData caseData = CaseData.builder().caseType("HearingRecordings").build();
        final DisposeDocumentsRemoteOperation disposeDocumentsRemoteOperation =
            mock(DisposeDocumentsRemoteOperation.class);

        disposeRemoteOperations.add(disposeDocumentsRemoteOperation);

        remoteDisposeService.remoteDeleteAll(caseData);

        verify(disposeDocumentsRemoteOperation, never()).delete(caseData);
    }

    @Test
    void shouldNotDeleteHearingsWhenNotHearingCase() {
        final CaseData caseData = CaseData.builder().caseType("NonHearingCase").build();
        final DisposeHearingsRemoteOperation disposeHearingsRemoteOperation =
            mock(DisposeHearingsRemoteOperation.class);

        disposeRemoteOperations.add(disposeHearingsRemoteOperation);

        remoteDisposeService.remoteDeleteAll(caseData);

        verify(disposeHearingsRemoteOperation, never()).delete(caseData);
    }

    @Test
    void shouldDeleteAllWhenNotHearingOrDocuments() {
        final CaseData caseData = CaseData.builder().caseType("OtherCaseType").build();

        final DisposeDocumentsRemoteOperation disposeDocumentsRemoteOperation =
            mock(DisposeDocumentsRemoteOperation.class);
        final DisposeRoleAssignmentsRemoteOperation disposeRoleAssignmentsRemoteOperation =
            mock(DisposeRoleAssignmentsRemoteOperation.class);
        final DisposeElasticsearchRemoteOperation disposeElasticsearchRemoteOperation =
            mock(DisposeElasticsearchRemoteOperation.class);
        final DisposeHearingsRemoteOperation disposeHearingsRemoteOperation =
            mock(DisposeHearingsRemoteOperation.class);
        final DisposeTasksRemoteOperation disposeTasksRemoteOperation =
            mock(DisposeTasksRemoteOperation.class);

        disposeRemoteOperations.add(disposeDocumentsRemoteOperation);
        disposeRemoteOperations.add(disposeRoleAssignmentsRemoteOperation);
        disposeRemoteOperations.add(disposeElasticsearchRemoteOperation);
        disposeRemoteOperations.add(disposeHearingsRemoteOperation);
        disposeRemoteOperations.add(disposeTasksRemoteOperation);

        remoteDisposeService.remoteDeleteAll(caseData);

        verify(disposeHearingsRemoteOperation, never()).delete(caseData);
        verify(disposeRoleAssignmentsRemoteOperation, times(1)).delete(caseData);
        verify(disposeDocumentsRemoteOperation, times(1)).delete(caseData);
        verify(disposeElasticsearchRemoteOperation, times(1)).delete(caseData);
        verify(disposeTasksRemoteOperation, times(1)).delete(caseData);
    }
}
