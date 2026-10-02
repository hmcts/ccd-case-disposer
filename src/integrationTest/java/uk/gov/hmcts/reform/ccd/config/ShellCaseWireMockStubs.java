package uk.gov.hmcts.reform.ccd.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.data.ShellCaseScenarios.ShellCaseResult;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@Component
public class ShellCaseWireMockStubs {

    public static final long ORIGINAL_CASE_REFERENCE = 1_504_259_907_353_529L;
    public static final String ORIGINAL_CASE_TYPE = "FT_MasterCaseType";
    public static final String SHELL_CASE_TYPE = "FT_ShellCaseType";
    public static final String DOCUMENT_ID = "11111111-1111-1111-1111-111111111111";

    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String MAPPING_PATH = "/api/retrieve-shell-mappings/" + ORIGINAL_CASE_TYPE;
    private static final String SEARCH_PATH = "/searchCases";
    private static final String ORIGINAL_CASE_PATH = "/cases/" + ORIGINAL_CASE_REFERENCE;
    private static final String DOCUMENT_HASH_PATH = "/cases/documents/" + DOCUMENT_ID + "/token";
    private static final String EVENT_TOKEN_PATH =
        "/case-types/" + SHELL_CASE_TYPE + "/event-triggers/createCase";
    private static final String CREATE_CASE_PATH = "/case-types/" + SHELL_CASE_TYPE + "/cases";

    public void setUp(WireMockServer wireMockServer, ShellCaseResult result) {
        wireMockServer.stubFor(get(urlPathEqualTo(MAPPING_PATH))
            .willReturn(response(result == ShellCaseResult.MAPPING_FAILURE ? 500 : 200, mappingResponse(result))));

        wireMockServer.stubFor(post(urlPathEqualTo(SEARCH_PATH))
            .willReturn(response(result == ShellCaseResult.SEARCH_FAILURE ? 500 : 200, searchResponse(result))));

        wireMockServer.stubFor(get(urlPathEqualTo(ORIGINAL_CASE_PATH))
            .willReturn(response(result == ShellCaseResult.ORIGINAL_CASE_FAILURE ? 500 : 200,
                originalCaseResponse())));

        wireMockServer.stubFor(get(urlPathEqualTo(DOCUMENT_HASH_PATH))
            .willReturn(response(result == ShellCaseResult.DOCUMENT_HASH_FAILURE ? 500 : 200,
                "{\"hashToken\":\"document-hash-token\"}")));

        String eventTokenResponse = result == ShellCaseResult.EVENT_TOKEN_FAILURE
            ? "{\"token\":\"\"}"
            : "{\"token\":\"create-event-token\"}";
        wireMockServer.stubFor(get(urlPathEqualTo(EVENT_TOKEN_PATH))
            .willReturn(response(200, eventTokenResponse)));

        wireMockServer.stubFor(post(urlPathEqualTo(CREATE_CASE_PATH))
            .willReturn(response(result == ShellCaseResult.CREATE_FAILURE ? 500 : 201, "{}")));
    }

    public String createCasePath() {
        return CREATE_CASE_PATH;
    }

    private ResponseDefinitionBuilder response(int status, String body) {
        return aResponse()
            .withStatus(status)
            .withHeader("Content-Type", JSON_CONTENT_TYPE)
            .withBody(body);
    }

    private String mappingResponse(ShellCaseResult result) {
        String mappedState = result == ShellCaseResult.CASE_STATE_EXCLUDED ? "ExcludedState" : "CaseCreated";
        return """
            {
              "shellCaseTypeID": "FT_ShellCaseType",
              "caseStates": [{"name": "%s", "stateCategory": "category"}],
              "shellCaseMappings": [
                {
                  "OriginatingCaseFieldName": "applicant.name",
                  "ShellCaseFieldName": "archivedApplicant.name"
                },
                {
                  "OriginatingCaseFieldName": "document",
                  "ShellCaseFieldName": "archivedDocument"
                }
              ]
            }
            """.formatted(mappedState);
    }

    private String searchResponse(ShellCaseResult result) {
        if (result == ShellCaseResult.SHELL_ALREADY_EXISTS) {
            return "{\"total\":1,\"cases\":[{\"id\":1600000000000001}]}";
        }
        return "{\"total\":0,\"cases\":[]}";
    }

    private String originalCaseResponse() {
        return """
            {
              "id": 1504259907353529,
              "case_type": "FT_MasterCaseType",
              "state": "CaseCreated",
              "data": {
                "applicant": {
                  "name": "Jane Smith"
                },
                "document": {
                  "document_url": "http://document-store/documents/11111111-1111-1111-1111-111111111111",
                  "document_binary_url":
                    "http://document-store/documents/11111111-1111-1111-1111-111111111111/binary",
                  "document_filename": "evidence.pdf"
                }
              }
            }
            """;
    }
}
