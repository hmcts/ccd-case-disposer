package uk.gov.hmcts.reform.ccd.service.remote;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoteDisposeServiceTest {

    @Spy
    private ArrayList<DisposeRemoteOperation> disposeRemoteOperations;

    @InjectMocks
    private RemoteDisposeService remoteDisposeService;

    @Test
    void shouldExecuteAllRemoteImplementations() {
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

        when(disposeDocumentsRemoteOperation.delete(any(CaseData.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        disposeRemoteOperations.add(disposeDocumentsRemoteOperation);

        when(disposeRoleAssignmentsRemoteOperation.delete(any(CaseData.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        disposeRemoteOperations.add(disposeRoleAssignmentsRemoteOperation);

        when(disposeElasticsearchRemoteOperation.delete(any(CaseData.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        disposeRemoteOperations.add(disposeElasticsearchRemoteOperation);

        when(disposeHearingsRemoteOperation.delete(any(CaseData.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        disposeRemoteOperations.add(disposeHearingsRemoteOperation);

        when(disposeTasksRemoteOperation.delete(any(CaseData.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        disposeRemoteOperations.add(disposeTasksRemoteOperation);

        remoteDisposeService.remoteDeleteAll(CaseData.builder().build()).join();

        verify(disposeDocumentsRemoteOperation, times(1))
            .delete(any(CaseData.class));
        verify(disposeRoleAssignmentsRemoteOperation, times(1))
            .delete(any(CaseData.class));
        verify(disposeElasticsearchRemoteOperation, times(1))
            .delete(any(CaseData.class));
        verify(disposeHearingsRemoteOperation, times(1))
            .delete(any(CaseData.class));
        verify(disposeTasksRemoteOperation, times(1))
            .delete(any(CaseData.class));
    }

}
