package uk.gov.hmcts.reform.ccd.shell.service;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.config.es.TestContainers;
import uk.gov.hmcts.reform.ccd.shell.data.CcdCaseResponse;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.SERVICE_AUTHORISATION_HEADER;

@SpringBootTest
@ActiveProfiles("integration")
@ComponentScan("uk.gov.hmcts.reform.ccd")
class OriginalCaseDataLoaderIntegrationTest extends TestContainers {

    private static final long CASE_REFERENCE = 1234567890123456L;
    private static final String CASE_PATH = "/cases/" + CASE_REFERENCE;
    private static final String SERVICE_TOKEN = "service-token";
    private static final String IDAM_TOKEN = "idam-token";

    @Inject
    private OriginalCaseDataLoader originalCaseDataLoader;

    @Inject
    private SecurityUtil securityUtil;

    @BeforeEach
    void setUp() {
        WIREMOCK_SERVER.resetAll();
        setField(securityUtil, "serviceAuthorization", SERVICE_TOKEN);
        setField(securityUtil, "idamClientToken", IDAM_TOKEN);
    }

    @Test
    void shouldLoadOriginalCaseData() {
        String responseBody = """
            {
              "id": 1234567890123456,
              "case_type": "FT_MasterCaseType",
              "state":"CaseCreated",
              "data": {
                "applicantName": "Jane Smith"
              }
            }
            """;

        WIREMOCK_SERVER.stubFor(get(urlPathEqualTo(CASE_PATH))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(responseBody)));

        CcdCaseResponse response = originalCaseDataLoader.load(CASE_REFERENCE);

        assertThat(response.id()).isEqualTo(CASE_REFERENCE);
        assertThat(response.caseType()).isEqualTo("FT_MasterCaseType");
        assertThat(response.data().get("applicantName").stringValue()).isEqualTo("Jane Smith");

        WIREMOCK_SERVER.verify(1, getRequestedFor(urlPathEqualTo(CASE_PATH))
            .withHeader(SERVICE_AUTHORISATION_HEADER, equalTo(SERVICE_TOKEN))
            .withHeader(AUTHORISATION_HEADER, equalTo(IDAM_TOKEN))
            .withHeader("Experimental", equalTo("true")));
    }
}
