package uk.gov.hmcts.reform.ccd.service.remote;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostRequest;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostResponse;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.RoleAssignmentDeletionException;
import uk.gov.hmcts.reform.ccd.service.remote.clients.RoleAssignmentClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.RoleDeletionRecordHolder;

@Service
@Slf4j
@RequiredArgsConstructor
public class DisposeRoleAssignmentsRemoteOperation implements DisposeRemoteOperation {

    private final RoleDeletionRecordHolder roleDeletionRecordHolder;
    private final RoleAssignmentClient roleAssignmentClient;
    private final SecurityUtil securityUtil;

    @Override
    public void delete(final CaseData caseData) {
        final String caseRef = caseData.getReference().toString();
        try {
            final RoleAssignmentsPostRequest roleAssignmentsDeleteRequest =
                new RoleAssignmentsPostRequest(caseRef);

            final ResponseEntity<RoleAssignmentsPostResponse> roleAssignmentsDeleteResponse =
                deleteRoleAssignment(roleAssignmentsDeleteRequest);

            roleDeletionRecordHolder.setCaseRolesDeletionResults(caseRef,
                                                                 roleAssignmentsDeleteResponse.getStatusCode().value());

            if (!roleAssignmentsDeleteResponse.getStatusCode().is2xxSuccessful()) {
                final String errorMessage = String
                    .format("Unexpected response code %d while deleting role assignments for case: %s",
                            roleAssignmentsDeleteResponse.getStatusCode().value(), caseData.getReference()
                    );

                throw new RoleAssignmentDeletionException(errorMessage);
            }

        } catch (final Exception ex) {
            final String errorMessage = String.format("Error deleting role assignments for case : %s", caseRef);
            log.error(errorMessage, ex);
            throw new RoleAssignmentDeletionException(errorMessage, ex);
        }
    }


    private ResponseEntity<RoleAssignmentsPostResponse> deleteRoleAssignment(final RoleAssignmentsPostRequest
                                                                         roleAssignmentsPostRequest) {
        return roleAssignmentClient.deleteRoleAssignment(
            securityUtil.getServiceAuthorization(),
            securityUtil.getIdamClientToken(),
            roleAssignmentsPostRequest
        );
    }

    @Override
    public String toString() {
        return "Role Assignment (AM) Disposer";
    }
}
