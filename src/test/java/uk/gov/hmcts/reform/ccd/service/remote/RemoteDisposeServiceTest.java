package uk.gov.hmcts.reform.ccd.service.remote;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

        disposeRemoteOperations.add(disposeDocumentsRemoteOperation);
        disposeRemoteOperations.add(disposeRoleAssignmentsRemoteOperation);
        disposeRemoteOperations.add(disposeElasticsearchRemoteOperation);

        remoteDisposeService.remoteDeleteAll(CaseData.builder().build());

        verify(disposeDocumentsRemoteOperation, times(1))
                .delete(any(CaseData.class));
        verify(disposeRoleAssignmentsRemoteOperation, times(1))
                .delete(any(CaseData.class));
        verify(disposeElasticsearchRemoteOperation, times(1))
                .delete(any(CaseData.class));
    }
}