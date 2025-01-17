package uk.gov.hmcts.reform.ccd.service.remote;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
        try {
            final DeleteTasksRequest tasksDeletePostRequest =
                new DeleteTasksRequest(new DeleteCaseTasksAction(caseRef));

            final ResponseEntity<Void> taskDeleteResponse = deleteTasks(tasksDeletePostRequest);

            tasksDeletionRecordHolder.setCaseTasksDeletionResults(caseRef, taskDeleteResponse.getStatusCode().value());

            if (!taskDeleteResponse.getStatusCode().is2xxSuccessful()) {
                final String errorMessage = String
                    .format("Unexpected response code %d while deleting tasks for case: %s",
                            taskDeleteResponse.getStatusCode().value(), caseData.getReference()
                    );

                throw new TasksDeletionException(errorMessage);
            }
        } catch (final Exception ex) {
            final String errorMessage = String.format("Error deleting tasks for case : %s", caseRef);
            log.error(errorMessage, ex);
            throw new TasksDeletionException(errorMessage, ex);
        }
    }

    private ResponseEntity<Void> deleteTasks(final DeleteTasksRequest tasksDeletePostRequest) {
        return tasksClient.deleteTasks(
            securityUtil.getServiceAuthorization(),
            securityUtil.getIdamClientToken(),
            tasksDeletePostRequest
        );
    }

    @Override
    public String toString() {
        return "Task Disposer";
    }
}
