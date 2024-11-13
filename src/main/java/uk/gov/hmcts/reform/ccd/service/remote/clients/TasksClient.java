package uk.gov.hmcts.reform.ccd.service.remote.clients;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.ccd.data.tm.DeleteTasksRequest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_TASKS_PATH;

@FeignClient(name = "tasksClient", url = "${ccd.tasks.host}")
public interface TasksClient {

    @PostMapping(
        value = DELETE_TASKS_PATH,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE)
    Response deleteTasks(
        @RequestHeader("ServiceAuthorization") String serviceAuthHeader,
        @RequestHeader(AUTHORISATION_HEADER) String authHeader,
        @RequestBody final DeleteTasksRequest tasksDeletePostRequest
    );

}
