package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.Gson;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.tm.DeleteCaseTasksAction;
import uk.gov.hmcts.reform.ccd.data.tm.DeleteTasksRequest;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.TasksDeletionRecordHolder;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_TASKS_PATH;

@DisplayName("dispose case documents")
@ExtendWith(MockitoExtension.class)
public class DisposeTaskRemoteOperationTest {

    @Mock
    private CcdRestClientBuilder ccdRestClientBuilder;

    @Mock
    private TasksDeletionRecordHolder taskDeletionRecordHolder;

    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private DisposeTasksRemoteOperation disposeTasksRemoteOperation;

    @BeforeEach
    void setUp() {
        doReturn("http://localhost").when(parameterResolver).getTasksHost();
    }

    final CaseData caseData = CaseData.builder().reference(1234567890123456L).build();

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("should post task delete remote dispose request successfully")
    void shouldPostDocumentsDeleteRemoteDisposeRequestSuccessfully() {

        final String jsonRequest = new Gson().toJson(new DeleteTasksRequest(
            new DeleteCaseTasksAction("1234567890123456")));
        final Response response = mock(Response.class);

        when(response.getStatus()).thenReturn(201);

        when(ccdRestClientBuilder.postRequestWithAllHeaders("http://localhost", DELETE_TASKS_PATH, jsonRequest)).thenReturn(response);

        disposeTasksRemoteOperation.delete(caseData);

        verify(taskDeletionRecordHolder, times(1)).setCaseTasksDeletionResults("1234567890123456",
                                                                               201);
        verify(ccdRestClientBuilder, times(1)).postRequestWithAllHeaders("http://localhost", DELETE_TASKS_PATH, jsonRequest);
    }
}
