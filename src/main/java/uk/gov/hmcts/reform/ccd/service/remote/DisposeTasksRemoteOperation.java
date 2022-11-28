package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.tm.TasksDeletePostRequest;
import uk.gov.hmcts.reform.ccd.exception.TasksDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.TasksDeletionRecordHolder;

import javax.ws.rs.core.Response;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_TASKS_PATH;

@Service
@Slf4j
public class DisposeTasksRemoteOperation implements DisposeRemoteOperation {

    private final ParameterResolver parameterResolver;

    private final Gson gson = new Gson();

    private TasksDeletionRecordHolder tasksDeletionRecordHolder;

    private final RestClientBuilder restClientBuilder;

    @Autowired
    public DisposeTasksRemoteOperation(final RestClientBuilder restClientBuilder,
                                       final ParameterResolver parameterResolver,
                                       final TasksDeletionRecordHolder tasksDeletionRecordHolder) {
        this.restClientBuilder = restClientBuilder;
        this.parameterResolver = parameterResolver;
        this.tasksDeletionRecordHolder = tasksDeletionRecordHolder;
    }

    @Override
    public void delete(final CaseData caseData) {
        final String caseRef = caseData.getReference().toString();
        try {
            final TasksDeletePostRequest tasksDeletePostRequest =
                    new TasksDeletePostRequest(caseRef);

            final String requestDeleteBody = gson.toJson(tasksDeletePostRequest);

            final Response roleAssignmentsDeleteResponse = deleteTasks(requestDeleteBody);

            logTasksDisposal(caseRef, roleAssignmentsDeleteResponse);

        } catch (final Exception ex) {
            final String errorMessage = String.format("Error deleting tasks for case : %s", caseRef);
            log.error(errorMessage, ex);
            throw new TasksDeletionException(errorMessage, ex);
        }
    }


    private void logTasksDisposal(final String caseRef, final Response taskDeleteResponse) {
        tasksDeletionRecordHolder.setCaseTasksDeletionResults(caseRef, taskDeleteResponse.getStatus());
    }

    @Async
    Response deleteTasks(final String requestDeleteBody) {
        return restClientBuilder
                .postRequestWithAllHeaders(parameterResolver.getTasksHost(),
                        DELETE_TASKS_PATH, requestDeleteBody);
    }
}
