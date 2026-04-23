package uk.gov.hmcts.reform.ccd.service.remote;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.data.am.QueryRequest;
import uk.gov.hmcts.reform.ccd.data.am.QueryResponse;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostRequest;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostResponse;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.RoleAssignmentDeletionException;
import uk.gov.hmcts.reform.ccd.service.remote.clients.RoleAssignmentClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.RoleDeletionRecordHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    @DisplayName("should post role assignment delete remote dispose request without query")
    void shouldPostRoleAssignmentsDeleteRemoteDisposeRequest() {
        final RoleAssignmentsPostResponse roleAssignmentsPostResponse = new RoleAssignmentsPostResponse();
        roleAssignmentsPostResponse.setRoleAssignmentResponse(List.of(mock(QueryResponse.class)));
        final ArgumentCaptor<RoleAssignmentsPostRequest> requestCaptor =
            ArgumentCaptor.forClass(RoleAssignmentsPostRequest.class);

        when(securityUtil.getServiceAuthorization()).thenReturn("some_cool_service_auth");
        when(securityUtil.getIdamClientToken()).thenReturn("some_cool_idam_token");

        when(roleAssignmentClient.deleteRoleAssignment(
            anyString(),
            anyString(),
            any(RoleAssignmentsPostRequest.class)
        ))
            .thenReturn(ResponseEntity.ok(roleAssignmentsPostResponse));

        disposeRoleAssignmentsRemoteOperation.delete(caseData);

        verify(roleDeletionRecordHolder, times(1)).setCaseRolesDeletionResults(
            "1234567890123456",
            200
        );
        verify(roleAssignmentClient, times(1)).deleteRoleAssignment(
            eq("some_cool_service_auth"),
            eq("some_cool_idam_token"),
            requestCaptor.capture()
        );

        final RoleAssignmentsPostRequest request = requestCaptor.getValue();
        assertThat(request.getQueryRequests()).hasSize(1);
        final QueryRequest queryRequest = request.getQueryRequests().get(0);
        assertThat(queryRequest.getAttributes()).containsEntry("caseId", List.of("1234567890123456"));
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
        )).thenReturn(ResponseEntity.status(500).body(roleAssignmentsPostResponse));

        assertThatExceptionOfType(RoleAssignmentDeletionException.class)
            .isThrownBy(() -> disposeRoleAssignmentsRemoteOperation.delete(caseData))
            .withMessage("Unexpected response code 500 while deleting role assignments for case: 1234567890123456");

        verify(roleDeletionRecordHolder, times(1)).setCaseRolesDeletionResults(
            "1234567890123456",
            500
        );
    }

    @Test
    void shouldThrowExceptionWhenDeleteRequestInvalid() {
        final RuntimeException cause = new RuntimeException("role assignment service unavailable");

        when(securityUtil.getServiceAuthorization()).thenReturn("some_cool_service_auth");
        when(securityUtil.getIdamClientToken()).thenReturn("some_cool_idam_token");

        doThrow(cause)
            .when(roleAssignmentClient)
            .deleteRoleAssignment(
                eq("some_cool_service_auth"),
                eq("some_cool_idam_token"),
                any(RoleAssignmentsPostRequest.class)
            );

        assertThatExceptionOfType(RoleAssignmentDeletionException.class)
            .isThrownBy(() -> disposeRoleAssignmentsRemoteOperation.delete(caseData))
            .withMessage("Error deleting role assignments for case : 1234567890123456")
            .withCause(cause);

        verifyNoInteractions(roleDeletionRecordHolder);
    }

    @Test
    void shouldAcceptAny2xxStatusCode() {
        final RoleAssignmentsPostResponse roleAssignmentsPostResponse = new RoleAssignmentsPostResponse();
        roleAssignmentsPostResponse.setRoleAssignmentResponse(List.of(mock(QueryResponse.class)));

        when(securityUtil.getServiceAuthorization()).thenReturn("some_cool_service_auth");
        when(securityUtil.getIdamClientToken()).thenReturn("some_cool_idam_token");
        when(roleAssignmentClient.deleteRoleAssignment(anyString(), anyString(), any(RoleAssignmentsPostRequest.class)))
            .thenReturn(ResponseEntity.status(204).body(roleAssignmentsPostResponse));

        disposeRoleAssignmentsRemoteOperation.delete(caseData);

        verify(roleDeletionRecordHolder, times(1)).setCaseRolesDeletionResults(
            "1234567890123456",
            204
        );
    }

    @Test
    void shouldWrapSecurityUtilFailures() {
        final RuntimeException cause = new RuntimeException("token lookup failed");

        when(securityUtil.getServiceAuthorization()).thenThrow(cause);

        assertThatExceptionOfType(RoleAssignmentDeletionException.class)
            .isThrownBy(() -> disposeRoleAssignmentsRemoteOperation.delete(caseData))
            .withMessage("Error deleting role assignments for case : 1234567890123456")
            .withCause(cause);

        verifyNoInteractions(roleDeletionRecordHolder);
    }
}
