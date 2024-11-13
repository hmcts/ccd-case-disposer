package uk.gov.hmcts.reform.ccd.service.remote;

import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.tm.DeleteTasksRequest;
import uk.gov.hmcts.reform.ccd.service.remote.clients.TasksClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.TasksDeletionRecordHolder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisposeTaskRemoteOperationTest {

    @Mock
    private TasksClient tasksClient;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private TasksDeletionRecordHolder tasksDeletionRecordHolder;

    @InjectMocks
    private DisposeTasksRemoteOperation disposeTasksRemoteOperation;

    final CaseData caseData = CaseData.builder().reference(1234567890123456L).build();

    @Test
    @DisplayName("should delete tasks successfully")
    void shouldDeleteTasksSuccessfully() {
        final Response response = mock(Response.class);

        when(securityUtil.getServiceAuthorization()).thenReturn("some_cool_service_auth");
        when(securityUtil.getIdamClientToken()).thenReturn("some_cool_idam_token");
        when(response.status()).thenReturn(HttpStatus.CREATED.value());
        when(tasksClient.deleteTasks(anyString(), anyString(), any(DeleteTasksRequest.class)))
            .thenReturn(response);

        disposeTasksRemoteOperation.delete(caseData);

        verify(tasksDeletionRecordHolder, times(1)).setCaseTasksDeletionResults(
            "1234567890123456",
            201
        );
        verify(tasksClient, times(1)).deleteTasks(
            eq("some_cool_service_auth"),
            eq("some_cool_idam_token"),
            any(DeleteTasksRequest.class)
        );
    }

    @Test
    void shouldThrowTasksDeletionExceptionWhenResponseStatusIsNot201() {
        CaseData caseData = CaseData.builder().reference(12345L).build();
        Response response = Response.builder()
            .status(400) // Non-201 response status
            .request(mock(Request.class))
            .build();

        when(tasksClient.deleteTasks(any(), any(), any(DeleteTasksRequest.class))).thenReturn(response);

        assertDoesNotThrow(() -> disposeTasksRemoteOperation.delete(caseData));
    }
}
