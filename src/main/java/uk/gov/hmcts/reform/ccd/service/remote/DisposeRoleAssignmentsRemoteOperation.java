package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.exception.RoleAssignmentDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.RoleDeletionRecordHolder;

import javax.ws.rs.core.Response;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_ROLE_PATH;

@Service
@Slf4j
@Qualifier("DisposeRoleAssignmentsRemoteOperation")
public class DisposeRoleAssignmentsRemoteOperation {

    private final ParameterResolver parameterResolver;

    private final Gson gson = new Gson();

    private RoleDeletionRecordHolder roleDeletionRecordHolder;

    private final RestClientBuilder restClientBuilder;

    @Autowired
    public DisposeRoleAssignmentsRemoteOperation(final RestClientBuilder restClientBuilder,
                                                 final ParameterResolver parameterResolver,
                                                 final RoleDeletionRecordHolder roleDeletionRecordHolder) {
        this.restClientBuilder = restClientBuilder;
        this.parameterResolver = parameterResolver;
        this.roleDeletionRecordHolder = roleDeletionRecordHolder;
    }

    public void postRoleAssignmentsDelete(String caseRef) {

        try {
            final RoleAssignmentsDeletePostRequest roleAssignmentsDeleteRequest =
                    new RoleAssignmentsDeletePostRequest(caseRef);

            final String requestBody = gson.toJson(roleAssignmentsDeleteRequest);

            final Response roleAssignmentsDeleteResponse = restClientBuilder
                    .postRequestWithAllHeaders(parameterResolver.getRoleAssignmentsHost(),
                            DELETE_ROLE_PATH,
                            requestBody);

            logRoleAssignmentsDisposal(caseRef, roleAssignmentsDeleteResponse);

        } catch (Exception ex) {
            final String errorMessage = String.format("Error deleting role assignments for case : %s", caseRef);
            log.error(errorMessage, ex);
            Thread.currentThread().interrupt();
            throw new RoleAssignmentDeletionException(errorMessage, ex);
        }
    }

    private void logRoleAssignmentsDisposal(final String caseRef, final Response roleAssignmentsDeleteResponse) {
        roleDeletionRecordHolder.setCaseRolesDeletionResults(caseRef, roleAssignmentsDeleteResponse.getStatus());
    }
}
