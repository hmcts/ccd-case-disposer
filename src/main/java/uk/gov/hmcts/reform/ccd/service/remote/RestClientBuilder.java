package uk.gov.hmcts.reform.ccd.service.remote;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.logging.LoggingFeature;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.SERVICE_AUTHORISATION_HEADER;

@Service
public class RestClientBuilder {

    private static final int CLIENT_READ_TIMEOUT = 30000;
    private static final int CLIENT_CONNECT_TIMEOUT = 5000;

    private final SecurityUtil securityUtil;
    private Client client;

    public RestClientBuilder(final SecurityUtil securityUtil) {
        this.securityUtil = securityUtil;
        getClient();
    }

    public String postRequest(final String baseUrl,
                              final String path,
                              final String body) {

        return client.register(new LoggingFeature())
                .target(baseUrl)
                .path(path)
                .request()
                .header(SERVICE_AUTHORISATION_HEADER, securityUtil.getServiceAuthorization())
                .post(Entity.json(body))
                .readEntity(String.class);
    }

    private Client getClient() {
        if (client == null) {
            final ClientConfig clientConfig = new ClientConfig()
                    .property(ClientProperties.READ_TIMEOUT, CLIENT_READ_TIMEOUT)
                    .property(ClientProperties.CONNECT_TIMEOUT, CLIENT_CONNECT_TIMEOUT);

            client = ClientBuilder.newClient(clientConfig);

            return client;
        }
        return client;
    }
}