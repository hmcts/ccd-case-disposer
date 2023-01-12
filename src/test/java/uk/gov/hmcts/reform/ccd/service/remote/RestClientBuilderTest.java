package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.SERVICE_AUTHORISATION_HEADER;

@ExtendWith(MockitoExtension.class)
class RestClientBuilderTest {

    @Mock
    private SecurityUtil securityUtil;

    @Test
    void shouldReturnRestClientWithServiceUthHeader() {
        final Client client = mock(Client.class);
        final WebTarget webTarget = mock(WebTarget.class);
        final Invocation.Builder builder = mock(Invocation.Builder.class);
        final Response response = mock(Response.class);

        final RestClientBuilder restClientBuilder = new RestClientBuilder(securityUtil);
        final DocumentsDeletePostRequest documentsDeleteRequest = new DocumentsDeletePostRequest("123");
        final Gson gson = new Gson();
        final String requestBody = gson.toJson(documentsDeleteRequest);

        setField(restClientBuilder, "client", client);

        doReturn("Bearer 12345").when(securityUtil).getServiceAuthorization();
        when(client.target(any(String.class))).thenReturn(webTarget);
        when(webTarget.path(any(String.class))).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);
        when(builder.header(SERVICE_AUTHORISATION_HEADER, "Bearer 12345")).thenReturn(builder);
        when(builder.post(Entity.json(requestBody))).thenReturn(response);
        when(response.readEntity(String.class)).thenReturn("Web client response");

        final String postResponse = restClientBuilder.postRequestWithServiceAuthHeader("http://localhost:9090", "/delete", requestBody);

        verify(response, times(1)).readEntity(String.class);
        assertThat(postResponse).isEqualTo("Web client response");
    }

    @Test
    void shouldReturnRestClientWithAllHeaders() {
        final Client client = mock(Client.class);
        final WebTarget webTarget = mock(WebTarget.class);
        final Invocation.Builder builder = mock(Invocation.Builder.class);
        final Response response = mock(Response.class);

        final RestClientBuilder restClientBuilder = new RestClientBuilder(securityUtil);
        final DocumentsDeletePostRequest documentsDeleteRequest = new DocumentsDeletePostRequest("123");
        final Gson gson = new Gson();
        final String requestBody = gson.toJson(documentsDeleteRequest);

        setField(restClientBuilder, "client", client);

        doReturn("Bearer 12345").when(securityUtil).getServiceAuthorization();
        doReturn("Bearer 5678").when(securityUtil).getIdamClientToken();
        when(client.target(any(String.class))).thenReturn(webTarget);
        when(webTarget.path(any(String.class))).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);
        when(builder.header(SERVICE_AUTHORISATION_HEADER, "Bearer 12345")).thenReturn(builder);
        when(builder.header(AUTHORISATION_HEADER, "Bearer 5678")).thenReturn(builder);
        when(builder.post(Entity.json(requestBody))).thenReturn(response);

        final Response postResponse = restClientBuilder.postRequestWithAllHeaders("http://localhost:9090", "/delete",
                requestBody);

        assertThat(postResponse).isEqualTo(response);
    }


    @Test
    void shouldGetClient() {
        final RestClientBuilder restClientBuilder = new RestClientBuilder(securityUtil);
        final Client client = restClientBuilder.getClient();

        assertThat(client.getConfiguration().getProperty(READ_TIMEOUT)).isEqualTo(60000);
        assertThat(client.getConfiguration().getProperty(CONNECT_TIMEOUT)).isEqualTo(60000);
    }
}