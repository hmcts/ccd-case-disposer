package uk.gov.hmcts.reform.ccd.service.remote;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DisposeTasksRemoteOperation implements DisposeRemoteOperation {

    private final SecurityUtil securityUtil;

    private TasksDeletionRecordHolder tasksDeletionRecordHolder;

    private final TasksClient tasksClient;

    @Autowired
    public DisposeTasksRemoteOperation(final TasksClient tasksClient,
                                       final SecurityUtil securityUtil,
                                       final TasksDeletionRecordHolder tasksDeletionRecordHolder) {
        this.tasksClient = tasksClient;
        this.securityUtil = securityUtil;
        this.tasksDeletionRecordHolder = tasksDeletionRecordHolder;
    }

    @SuppressWarnings("java:S1135")
    @Override
    public void delete(final CaseData caseData) {
        final String caseRef = caseData.getReference().toString();
        try {
            final DeleteTasksRequest tasksDeletePostRequest =
                new DeleteTasksRequest(new DeleteCaseTasksAction(caseRef));


            final Response taskDeleteResponse = deleteTasks(tasksDeletePostRequest);

            tasksDeletionRecordHolder.setCaseTasksDeletionResults(caseRef, taskDeleteResponse.status());

            if (taskDeleteResponse.status() != 201) {
                final String errorMessage = String
                    .format("Unexpected response code %d while deleting tasks for case: %s",
                            taskDeleteResponse.status(), caseData.getReference()
                    );

                throw new TasksDeletionException(errorMessage);
            }
        } catch (final Exception ex) {
            // TODO: we need to re-throw the exception here once task deletion endpoint is enabled in PROD.
            // TODO: rethrowing the exception will prevent the case deletion in CCD
            final String errorMessage = String.format("Error deleting tasks for case : %s", caseRef);
            log.error(errorMessage, ex);
        }
    }

    Response deleteTasks(final DeleteTasksRequest tasksDeletePostRequest) {
        return tasksClient.deleteTasks(
            securityUtil.getServiceAuthorization(),
            securityUtil.getIdamClientToken(),
            tasksDeletePostRequest
        );
    }
}
