package uk.gov.hmcts.reform.ccd.service.remote;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.data.am.QueryResponse;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostRequest;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostResponse;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.RoleAssignmentDeletionException;
import uk.gov.hmcts.reform.ccd.service.remote.clients.RoleAssignmentClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.RoleDeletionRecordHolder;

import java.util.List;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("dispose case role assignments")
@ExtendWith(MockitoExtension.class)
class DisposeRoleAssignmentsRemoteOperationTest {

    @Mock
    private RoleAssignmentClient roleAssignmentClient;

    @Mock
    private RoleDeletionRecordHolder roleDeletionRecordHolder;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private DisposeRoleAssignmentsRemoteOperation disposeRoleAssignmentsRemoteOperation;

    final CaseData caseData = CaseData.builder().reference(1234567890123456L).build();

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("should post role assignment delete remote dispose request without query")
    void shouldPostRoleAssignmentsDeleteRemoteDisposeRequest() {

        final RoleAssignmentsPostResponse roleAssignmentsPostResponse = new RoleAssignmentsPostResponse();
        roleAssignmentsPostResponse.setRoleAssignmentResponse(List.of(mock(QueryResponse.class)));

        when(securityUtil.getServiceAuthorization()).thenReturn("some_cool_service_auth");
        when(securityUtil.getIdamClientToken()).thenReturn("some_cool_idam_token");

        when(roleAssignmentClient.deleteRoleAssignment(
            anyString(),
            anyString(),
            any(RoleAssignmentsPostRequest.class)
        ))
            .thenReturn(ResponseEntity.ok(roleAssignmentsPostResponse));

        disposeRoleAssignmentsRemoteOperation.delete(caseData).join();

        verify(roleDeletionRecordHolder, times(1)).setCaseRolesDeletionResults(
            "1234567890123456",
            200
        );
        verify(roleAssignmentClient, times(1)).deleteRoleAssignment(
            eq("some_cool_service_auth"),
            eq("some_cool_idam_token"),
            any()
        );
    }

    @Test
    void shouldThrowExceptionWhenQueryRequestInvalid() {
        final RoleAssignmentsPostResponse roleAssignmentsPostResponse = new RoleAssignmentsPostResponse();
        roleAssignmentsPostResponse.setRoleAssignmentResponse(List.of(mock(QueryResponse.class)));

        when(securityUtil.getServiceAuthorization()).thenReturn("some_cool_service_auth");
        when(securityUtil.getIdamClientToken()).thenReturn("some_cool_idam_token");

        when(roleAssignmentClient.deleteRoleAssignment(
            anyString(),
            anyString(),
            any(RoleAssignmentsPostRequest.class)
        ))
            .thenReturn(ResponseEntity.status(500).body(roleAssignmentsPostResponse));

        CompletionException ex = assertThrows(CompletionException.class, () ->
            disposeRoleAssignmentsRemoteOperation.delete(caseData).join()
        );
        assertThat(ex.getCause())
            .isInstanceOf(RoleAssignmentDeletionException.class)
            .hasMessage("Error deleting role assignments for case : 1234567890123456");
    }


    @Test
    void shouldThrowExceptionWhenDeleteRequestInvalid() {
        final String caseRef = "1234567890123456";

        doThrow(new RoleAssignmentDeletionException(caseRef))
            .when(roleAssignmentClient)
            .deleteRoleAssignment(anyString(), anyString(), any(RoleAssignmentsPostRequest.class));

        CompletionException ex = assertThrows(CompletionException.class, () ->
            disposeRoleAssignmentsRemoteOperation.delete(caseData).join()
        );
        assertThat(ex.getCause())
            .isInstanceOf(RoleAssignmentDeletionException.class)
            .hasMessage("Error deleting role assignments for case : 1234567890123456");
    }
}
