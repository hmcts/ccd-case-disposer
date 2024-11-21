package uk.gov.hmcts.reform.ccd.service.remote.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostRequest;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_ROLE_PATH;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.MEDIA_TYPE_POST_DELETE_ASSIGNMENTS;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.SERVICE_AUTHORISATION_HEADER;

@FeignClient(name = "roleAssignmentClient", url = "${remote.role.assignment.host}")
public interface RoleAssignmentClient {

    @PostMapping(
        value = DELETE_ROLE_PATH,
        produces = MEDIA_TYPE_POST_DELETE_ASSIGNMENTS,
        consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<RoleAssignmentsPostResponse> deleteRoleAssignment(
        @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthHeader,
        @RequestHeader(AUTHORISATION_HEADER) String authHeader,
        @RequestBody final RoleAssignmentsPostRequest roleAssignmentsPostRequest
    );

}
