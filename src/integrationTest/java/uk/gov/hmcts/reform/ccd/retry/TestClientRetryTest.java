package uk.gov.hmcts.reform.ccd.retry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.data.TestDataProvider;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.service.remote.clients.DocumentClient;

@SpringBootTest
@ActiveProfiles("test")
@ComponentScan({"uk.gov.hmcts.reform.ccd"})
class TestClientRetryTest  extends TestDataProvider {

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

    }
}
