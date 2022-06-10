package uk.gov.hmcts.reform.ccd.service.remote;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.RoleDeletionRecordHolder;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("dispose case role assignments")
@ExtendWith(MockitoExtension.class)
class DisposeRoleAssignmentsRemoteOperationTest {

    @Captor
    ArgumentCaptor<HttpRequest> captor;

    @Mock
    private HttpClient httpClient;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private RoleDeletionRecordHolder roleDeletionRecordHolder;

    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private DisposeRoleAssignmentsRemoteOperation disposeRoleAssignmentsRemoteOperation;

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("should post role assignment delete remote dispose request")
    void shouldPostRoleAssignmentsDeleteRemoteDisposeRequest() throws IOException, InterruptedException {

        doReturn("http://localhost")
            .when(parameterResolver).getRoleAssignmentsHost();

        doReturn("Bearer 1234567890").when(securityUtil).getIdamClientToken();
        doReturn("Bearer 12345").when(securityUtil).getServiceAuthorization();

        HttpResponse httpResponse = mock(HttpResponse.class);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        disposeRoleAssignmentsRemoteOperation.postRoleAssignmentsDelete("1234567890123456");

        verify(httpClient).send(captor.capture(), any());

        assertThat(captor.getValue().uri().getPath(), is(equalTo("/am/role-assignments/query/delete")));
        assertThat(captor.getValue().headers().map().size(), is(equalTo(3)));
        assertThat(captor.getValue().headers().map().get("Authorization").get(0), is(equalTo("Bearer 1234567890")));
        assertThat(captor.getValue().headers().map().get("ServiceAuthorization").get(0), is(equalTo("Bearer 12345")));
        assertThat(captor.getValue().headers().map().get("Content-Type").get(0), is(equalTo("application/json")));

        HttpRequest.BodyPublisher bodyPublisher = captor.getValue().bodyPublisher().get();
        assertThat(bodyPublisher.contentLength(), is(equalTo(66L)));

        verify(roleDeletionRecordHolder, times(1))
            .setCaseRolesDeletionResults(eq("1234567890123456"), anyInt());
    }

}
