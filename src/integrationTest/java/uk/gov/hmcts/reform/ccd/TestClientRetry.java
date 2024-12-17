package uk.gov.hmcts.reform.ccd;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.service.remote.clients.DocumentClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_DOCUMENT_PATH;

@SpringBootTest
@ActiveProfiles("test")
@ComponentScan({"uk.gov.hmcts.reform.ccd"})
class TestClientRetry {

    protected static final WireMockServer WIREMOCK_SERVER = new WireMockServer(4603);

    static {
        if (!WIREMOCK_SERVER.isRunning()) {
            WIREMOCK_SERVER.start();
        }

        WIREMOCK_SERVER.stubFor(post(urlPathMatching(DELETE_DOCUMENT_PATH))
                                    .willReturn(aResponse()
                                                    .withStatus(501)
                                                    .withBody("Connection refused error custom message")));
    }


    @Autowired
    private DocumentClient documentClient;

    @Test
    void testFeignClientRetry() {

        DocumentsDeletePostRequest request = new DocumentsDeletePostRequest("12345");

        try {
            documentClient.deleteDocument("serviceAuthHeader", request);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        WIREMOCK_SERVER.verify(3, postRequestedFor(urlPathMatching(DELETE_DOCUMENT_PATH)));

    }
}
