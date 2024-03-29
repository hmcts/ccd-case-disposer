package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.Gson;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostRequest;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostResponse;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.RoleAssignmentDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.RoleDeletionRecordHolder;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_ROLE_PATH;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.QUERY_ROLE_PATH;

@Service
@Slf4j
public class DisposeRoleAssignmentsRemoteOperation implements DisposeRemoteOperation {

    private final ParameterResolver parameterResolver;

    private final Gson gson = new Gson();

    private RoleDeletionRecordHolder roleDeletionRecordHolder;

    private final CcdRestClientBuilder ccdRestClientBuilder;

    @Autowired
    public DisposeRoleAssignmentsRemoteOperation(final CcdRestClientBuilder ccdRestClientBuilder,
                                                 final ParameterResolver parameterResolver,
                                                 final RoleDeletionRecordHolder roleDeletionRecordHolder) {
        this.ccdRestClientBuilder = ccdRestClientBuilder;
        this.parameterResolver = parameterResolver;
        this.roleDeletionRecordHolder = roleDeletionRecordHolder;
    }

    @Override
    public void delete(final CaseData caseData) {
        final String caseRef = caseData.getReference().toString();
        try {
            if (!parameterResolver.getCheckCaseRolesExist()
                ||
                (parameterResolver.getCheckCaseRolesExist() && hasRoleAssignments(caseRef))) {

                final RoleAssignmentsPostRequest roleAssignmentsDeleteRequest =
                    new RoleAssignmentsPostRequest(caseRef);

                final String requestDeleteBody = gson.toJson(roleAssignmentsDeleteRequest);

                final Response roleAssignmentsDeleteResponse = deleteRoleAssignment(requestDeleteBody);

                logRoleAssignmentsDisposal(caseRef, roleAssignmentsDeleteResponse);
                roleAssignmentsDeleteResponse.close();
            }

        } catch (final Exception ex) {
            final String errorMessage = String.format("Error deleting role assignments for case : %s", caseRef);
            log.error(errorMessage, ex);
            throw new RoleAssignmentDeletionException(errorMessage, ex);
        }
    }

    private boolean hasRoleAssignments(String caseRef) {

        final RoleAssignmentsPostRequest roleAssignmentsQueryRequest =
            new RoleAssignmentsPostRequest(caseRef);

        final String requestQueryBody = gson.toJson(roleAssignmentsQueryRequest);

        final Response roleAssignmentsQueryResponse = ccdRestClientBuilder
            .postRequestWithRoleAssignmentFetchContentType(parameterResolver.getRoleAssignmentsHost(),
                                                           QUERY_ROLE_PATH, requestQueryBody);

        if (roleAssignmentsQueryResponse.getStatus() == HttpStatus.OK.value()) {
            RoleAssignmentsPostResponse roleAssignmentsResponse =
                roleAssignmentsQueryResponse.readEntity(RoleAssignmentsPostResponse.class);

            if (!CollectionUtils.isEmpty(roleAssignmentsResponse.getRoleAssignmentResponse())
            ) {
                log.info("Found {} role(s) for case : {}, calling AM role(s) delete endpoint to remove.",
                         roleAssignmentsResponse.getRoleAssignmentResponse().size(), caseRef);
                roleAssignmentsQueryResponse.close();
                return true;

            } else {
                log.info("No roles found for case : {}, skipping AM roles delete endpoint.", caseRef);
                logRoleAssignmentsDisposal(caseRef, roleAssignmentsQueryResponse);
                roleAssignmentsQueryResponse.close();
                return false;
            }

        } else {
            logRoleAssignmentsDisposal(caseRef, roleAssignmentsQueryResponse);
            roleAssignmentsQueryResponse.close();
            throw new RoleAssignmentDeletionException("Unable to get case assignment roles.");
        }
    }

    private void logRoleAssignmentsDisposal(final String caseRef, final Response roleAssignmentsDeleteResponse) {
        roleDeletionRecordHolder.setCaseRolesDeletionResults(caseRef, roleAssignmentsDeleteResponse.getStatus());
    }

    @Async
    Response deleteRoleAssignment(final String requestDeleteBody) {
        return ccdRestClientBuilder
                .postRequestWithAllHeaders(parameterResolver.getRoleAssignmentsHost(),
                        DELETE_ROLE_PATH, requestDeleteBody);
    }
}
