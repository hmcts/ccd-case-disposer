package uk.gov.hmcts.reform.ccd.service.remote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("dispose case artifacts calls")
class DisposeCaseRemoteOperationTest {

    @Captor
    ArgumentCaptor<HttpRequest> captor;

    @Mock
    private HttpClient httpClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private ParameterResolver parameterResolver;

    private DisposeCaseRemoteOperation disposeCaseRemoteOperation;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        MockitoAnnotations.openMocks(this);

        doReturn("{}").when(objectMapper).writeValueAsString(any());
        doReturn("Bearer 12345").when(securityUtil).getServiceAuthorization();
        doReturn("http://localhost/documents/delete").when(parameterResolver).getDocumentsDeleteUrl();

        disposeCaseRemoteOperation = new DisposeCaseRemoteOperation(securityUtil, httpClient,
                                                                    objectMapper, parameterResolver);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("should post documents delete remote dispose request without anomaly")
    void shouldPostDocumentsDeleteRemoteDisposeRequestWithoutAnomaly() throws IOException, InterruptedException {

        HttpResponse httpResponse = mock(HttpResponse.class);

        when(httpResponse.body()).thenReturn("body");

        CaseDocumentsDeletionResults caseDocumentsDeletionResults = new CaseDocumentsDeletionResults();
        caseDocumentsDeletionResults.setCaseDocumentsFound(1);
        caseDocumentsDeletionResults.setMarkedForDeletion(1);

        when(objectMapper.readValue(anyString(),
                                    eq(CaseDocumentsDeletionResults.class))).thenReturn(caseDocumentsDeletionResults);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        disposeCaseRemoteOperation.postDocumentsDelete("1234567890123456");

        verify(httpClient).send(captor.capture(),any());

        assertThat(captor.getValue().uri().getPath(), is(equalTo("/documents/delete")));
        assertThat(captor.getValue().headers().map().size(), is(equalTo(2)));
        assertThat(captor.getValue().headers().map().get("ServiceAuthorization").get(0), is(equalTo("Bearer 12345")));
        assertThat(captor.getValue().headers().map().get("Content-Type").get(0), is(equalTo("application/json")));

        HttpRequest.BodyPublisher bodyPublisher = captor.getValue().bodyPublisher().get();
        assertThat(bodyPublisher.contentLength(), is(equalTo(2L)));
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("should post documents delete remote dispose request with anomaly")
    void shouldPostDocumentsDeleteRemoteDisposeRequestWithAnomaly() throws IOException, InterruptedException {

        HttpResponse httpResponse = mock(HttpResponse.class);

        when(httpResponse.body()).thenReturn("body");

        CaseDocumentsDeletionResults caseDocumentsDeletionResults = new CaseDocumentsDeletionResults();
        caseDocumentsDeletionResults.setCaseDocumentsFound(1);
        caseDocumentsDeletionResults.setMarkedForDeletion(0);

        when(objectMapper.readValue(anyString(),
                                    eq(CaseDocumentsDeletionResults.class))).thenReturn(caseDocumentsDeletionResults);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        disposeCaseRemoteOperation.postDocumentsDelete("1234567890123456");

        verify(httpClient).send(captor.capture(),any());
    }

}
