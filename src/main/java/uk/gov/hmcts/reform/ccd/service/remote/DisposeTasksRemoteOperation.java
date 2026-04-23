package uk.gov.hmcts.reform.ccd.service.remote;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.tm.DeleteCaseTasksAction;
import uk.gov.hmcts.reform.ccd.data.tm.DeleteTasksRequest;
import uk.gov.hmcts.reform.ccd.exception.TasksDeletionException;
import uk.gov.hmcts.reform.ccd.service.remote.clients.TasksClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.TasksDeletionRecordHolder;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "remote.tasks.enabled", havingValue = "true")
public class DisposeTasksRemoteOperation implements DisposeRemoteOperation {

    private final SecurityUtil securityUtil;
    private final TasksDeletionRecordHolder tasksDeletionRecordHolder;
    private final TasksClient tasksClient;

    @Override
    public void delete(final CaseData caseData) {
        final String caseRef = caseData.getReference().toString();

        final DeleteTasksRequest tasksDeletePostRequest =
            new DeleteTasksRequest(new DeleteCaseTasksAction(caseRef));

        final ResponseEntity<Void> taskDeleteResponse;
        try {
            taskDeleteResponse = deleteTasks(tasksDeletePostRequest);
        } catch (final Exception ex) {
            log.error("Error deleting tasks for case : {}", caseRef, ex);
            throw new TasksDeletionException(caseRef, ex);
        }

        final HttpStatusCode statusCode = taskDeleteResponse.getStatusCode();
        tasksDeletionRecordHolder.setCaseTasksDeletionResults(caseRef, statusCode.value());

        if (!statusCode.is2xxSuccessful()) {
            log.error("Unexpected response code {} while deleting tasks for case: {}", statusCode.value(), caseRef);
            throw new TasksDeletionException(statusCode.value(), caseRef);
        }
    }

    private ResponseEntity<Void> deleteTasks(final DeleteTasksRequest tasksDeletePostRequest) {
        return tasksClient.deleteTasks(
            securityUtil.getServiceAuthorization(),
            securityUtil.getIdamClientToken(),
            tasksDeletePostRequest
        );
    }
}
