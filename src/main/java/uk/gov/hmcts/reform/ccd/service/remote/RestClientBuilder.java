package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.common.annotations.VisibleForTesting;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.SERVICE_AUTHORISATION_HEADER;

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