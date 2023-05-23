package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.common.annotations.VisibleForTesting;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.MEDIATYPE_ROLE_FETCH;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.SERVICE_AUTHORISATION_HEADER;

@Slf4j
@Service
public class RestClientBuilder {

    private static final int CLIENT_READ_TIMEOUT = 60000;
    private static final int CLIENT_CONNECT_TIMEOUT = 60000;

    private final SecurityUtil securityUtil;
    private Client client;

    public RestClientBuilder(final SecurityUtil securityUtil) {
        this.securityUtil = securityUtil;
        client = getClient();
    }

    public String postRequestWithServiceAuthHeader(final String baseUrl,
                                                   final String path,
                                                   final String body) {

        return client
            .target(baseUrl)
            .path(path)
            .request()
            .header(SERVICE_AUTHORISATION_HEADER, securityUtil.getServiceAuthorization())
            .post(Entity.json(body))
            .readEntity(String.class);
    }

    public Response postRequestWithAllHeaders(final String baseUrl,
                                              final String path,
                                              final String body) {

        return client
            .target(baseUrl)
            .path(path)
            .request()
            .header(SERVICE_AUTHORISATION_HEADER, securityUtil.getServiceAuthorization())
            .header(AUTHORISATION_HEADER, securityUtil.getIdamClientToken())
            .post(Entity.json(body));
    }

    public Response postRequestWithRoleAssignmentFetchContentType(final String baseUrl,
                                                                  final String path,
                                                                  final String body) {

        return client
            .target(baseUrl)
            .path(path)
            .request()
            .header(SERVICE_AUTHORISATION_HEADER, securityUtil.getServiceAuthorization())
            .header(AUTHORISATION_HEADER, securityUtil.getIdamClientToken())
            .post(Entity.entity(body, MediaType.valueOf(MEDIATYPE_ROLE_FETCH)));
    }

    // Client should be closed in finally block or used in try-with-resource
    @SuppressWarnings("java:S2095")
    @VisibleForTesting
    protected Client getClient() {
        final ClientConfig clientConfig = new ClientConfig()
            .property(READ_TIMEOUT, CLIENT_READ_TIMEOUT)
            .property(CONNECT_TIMEOUT, CLIENT_CONNECT_TIMEOUT);

        return ClientBuilder
            .newClient(clientConfig)
            .register(new LoggingFeature());
    }
}
