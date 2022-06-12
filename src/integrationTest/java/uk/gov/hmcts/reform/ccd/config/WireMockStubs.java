package uk.gov.hmcts.reform.ccd.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.gson.Gson;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static feign.form.ContentProcessor.CONTENT_TYPE_HEADER;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.DOCUMENT_DELETE;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.ROLE_DELETE;

@Configuration
public class WireMockStubs {

    private static final String WIREMOCK_S2S_ENDPOINT = "/lease";
    private static final String WIREMOCK_TOKEN_ENDPOINT = "/o/token";

    private static final String WIREMOCK_TOKEN_RESULT = String.format(
        "{\"access_token\":\"TOKEN\",\"token_type\":\"Bearer\","
            + "\"scope\": \"openid profile roles\",\"expires_in\":28800}");

    private static final String JSON_RESPONSE = "application/json;charset=UTF-8";

    private static final String DOCUMENTS_DELETE_PATH = "/documents/delete";
    private static final String ROLES_DELETE_PATH = "/am/role-assignments/query/delete";

    public void setUpStubs(final WireMockServer wireMockServer) {
        setupDeleteDocumentsStub(wireMockServer);
        setupDeleteRolesStub(wireMockServer);
        setupServiceAuthorisationStub(wireMockServer);
        setupAuthorisationStub(wireMockServer);
    }

    private void setupDeleteDocumentsStub(final WireMockServer wireMockServer) {
        DOCUMENT_DELETE.entrySet().forEach(entry ->
                wireMockServer.stubFor(post(urlPathMatching(DOCUMENTS_DELETE_PATH))
                        .withRequestBody(equalToJson(new Gson()
                                .toJson(new DocumentsDeletePostRequest(entry.getKey()))))
                        .willReturn(aResponse()
                                .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                .withBody(new Gson()
                                        .toJson(entry.getValue()))
                                .withStatus(200))));
    }

    private void setupDeleteRolesStub(final WireMockServer wireMockServer) {
        ROLE_DELETE.entrySet().forEach(entry ->
               wireMockServer.stubFor(post(urlPathMatching(ROLES_DELETE_PATH))
                          .withRequestBody(equalToJson(new Gson()
                                   .toJson(new RoleAssignmentsDeletePostRequest(entry.getKey()))))
                          .willReturn(aResponse()
                                  .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                  .withStatus(200))));
    }

    public void setupServiceAuthorisationStub(final WireMockServer wireMockServer) {
        wireMockServer.stubFor(post(urlPathMatching(WIREMOCK_S2S_ENDPOINT))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                        .withStatus(200)
                        .withBody("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6I"
                                + "kpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ."
                                + "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")));
    }

    public void setupAuthorisationStub(final WireMockServer wireMockServer) {
        wireMockServer.stubFor(post(WIREMOCK_TOKEN_ENDPOINT)
               .willReturn(aResponse()
                       .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                       .withBody(WIREMOCK_TOKEN_RESULT)
                       .withStatus(200)));
    }

}
