package uk.gov.hmcts.reform.ccd.service.remote.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostResponse;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsPostRequest;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.MEDIATYPE_ROLE_FETCH;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.QUERY_ROLE_PATH;

@FeignClient(name = "roleAssignmentClient", url = "${ccd.role.assignment.host}")
public interface RoleAssignmentClient {

    @PostMapping(
        value = QUERY_ROLE_PATH,
        produces = MEDIATYPE_ROLE_FETCH,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RoleAssignmentsPostResponse> getRoleAssignment(
        @RequestHeader("ServiceAuthorization") String serviceAuthHeader,
        @RequestHeader("Authorization") String authHeader,
        @RequestBody final RoleAssignmentsPostRequest roleAssignmentsPostRequest
    );

}
