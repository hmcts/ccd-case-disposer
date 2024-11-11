package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.Gson;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.tm.DeleteCaseTasksAction;
import uk.gov.hmcts.reform.ccd.data.tm.DeleteTasksRequest;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.TasksDeletionRecordHolder;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_TASKS_PATH;

@Service
@Slf4j
public class DisposeTasksRemoteOperation implements DisposeRemoteOperation {

    private final ParameterResolver parameterResolver;

    private final Gson gson = new Gson();

    private TasksDeletionRecordHolder tasksDeletionRecordHolder;

    private final CcdRestClientBuilder ccdRestClientBuilder;

    @Autowired
    public DisposeTasksRemoteOperation(final CcdRestClientBuilder ccdRestClientBuilder,
                                       final ParameterResolver parameterResolver,
                                       final TasksDeletionRecordHolder tasksDeletionRecordHolder) {
        this.ccdRestClientBuilder = ccdRestClientBuilder;
        this.parameterResolver = parameterResolver;
        this.tasksDeletionRecordHolder = tasksDeletionRecordHolder;
    }

    @Override
    public void delete(final CaseData caseData) {
        final String caseRef = caseData.getReference().toString();
        try {
            final DeleteTasksRequest tasksDeletePostRequest =
                    new DeleteTasksRequest(new DeleteCaseTasksAction(caseRef));

            final String requestDeleteBody = gson.toJson(tasksDeletePostRequest);

            final Response taskDeleteResponse = deleteTasks(requestDeleteBody);

            logTasksDisposal(caseRef, taskDeleteResponse);

            if (taskDeleteResponse.getStatus() != 201) {
                log.error("Error in deleting tasks for case : Case Ref = {} ", caseRef);
            }

        } catch (final Exception ex) {
            final String errorMessage = String.format("Error deleting tasks for case : %s", caseRef);
            log.error(errorMessage, ex);
        }
    }


    private void logTasksDisposal(final String caseRef, final Response taskDeleteResponse) {
        log.info("logTasksDisposal in  tasks for case : Case Ref = {}  and status : Status = {}",
                 caseRef,taskDeleteResponse.getStatus());
        tasksDeletionRecordHolder.setCaseTasksDeletionResults(caseRef, taskDeleteResponse.getStatus());
    }


    Response deleteTasks(final String requestDeleteBody) {
        return ccdRestClientBuilder
                .postRequestWithAllHeaders(parameterResolver.getTasksHost(),
                                           DELETE_TASKS_PATH, requestDeleteBody);
    }
}
