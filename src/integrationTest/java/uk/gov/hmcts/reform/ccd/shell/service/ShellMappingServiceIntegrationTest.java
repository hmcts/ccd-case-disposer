package uk.gov.hmcts.reform.ccd.shell.service;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.config.es.TestContainers;
import uk.gov.hmcts.reform.ccd.shell.model.ShellMappingResponse;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration")
@ComponentScan({"uk.gov.hmcts.reform.ccd"})
class ShellMappingServiceIntegrationTest extends TestContainers {

    private static final String SHELL_MAPPING_PATH = "/api/retrieve-shell-mappings/";

    @Inject
    private ShellMappingService shellMappingService;

    @BeforeEach
    void setUp() {
        WIREMOCK_SERVER.resetAll();
    }

    @Test
    void shouldLoadMappingsForSingleCaseType() {
        String caseTypeId = "FT_MasterCaseType";
        String body = "{"
            + "\"shellCaseTypeID\":\"FT_ShellCaseType\","
            + "\"shellCaseMappings\":[{"
            + "\"OriginatingCaseFieldName\":\"applicantName\","
            + "\"ShellCaseFieldName\":\"shellApplicantName\""
            + "}]}";

        WIREMOCK_SERVER.stubFor(get(urlPathEqualTo(SHELL_MAPPING_PATH + caseTypeId))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(body)));

        ShellMappingResponse response = shellMappingService.loadMappings(caseTypeId);

        assertThat(response)
            .isNotNull()
            .extracting(
                ShellMappingResponse::getShellCaseTypeID,
                r -> r.getShellCaseMappings().size(),
                r -> r.getShellCaseMappings().getFirst().getOriginatingCaseFieldName(),
                r -> r.getShellCaseMappings().getFirst().getShellCaseFieldName())
            .containsExactly("FT_ShellCaseType", 1, "applicantName", "shellApplicantName");

        WIREMOCK_SERVER.verify(1, getRequestedFor(urlPathEqualTo(SHELL_MAPPING_PATH + caseTypeId)));
    }

    @Test
    void shouldGetMappingsForEachCaseType() {
        String firstCaseType = "FT_MasterCaseType";
        String secondCaseType = "FT_OtherCaseType";

        WIREMOCK_SERVER.stubFor(get(urlPathEqualTo(SHELL_MAPPING_PATH + firstCaseType))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody("{\"shellCaseTypeID\":\"FT_Shell_Master\",\"shellCaseMappings\":[]}")));

        WIREMOCK_SERVER.stubFor(get(urlPathEqualTo(SHELL_MAPPING_PATH + secondCaseType))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody("{\"shellCaseTypeID\":\"FT_Shell_Other\",\"shellCaseMappings\":[]}")));

        Map<String, ShellMappingResponse> result = shellMappingService
            .getShellMappings(List.of(firstCaseType, secondCaseType));

        assertThat(result)
            .hasSize(2)
            .containsKeys(firstCaseType, secondCaseType)
            .extracting(
                m -> m.get(firstCaseType).getShellCaseTypeID(),
                m -> m.get(secondCaseType).getShellCaseTypeID())
            .containsExactly("FT_Shell_Master", "FT_Shell_Other");

        WIREMOCK_SERVER.verify(1, getRequestedFor(urlPathEqualTo(SHELL_MAPPING_PATH + firstCaseType)));
        WIREMOCK_SERVER.verify(1, getRequestedFor(urlPathEqualTo(SHELL_MAPPING_PATH + secondCaseType)));
    }

    @Test
    void shouldReturnEmptyMapWhenCaseTypeListIsEmpty() {
        Map<String, ShellMappingResponse> result = shellMappingService.getShellMappings(List.of());

        assertThat(result).isEmpty();
        WIREMOCK_SERVER.verify(0, getRequestedFor(urlPathMatching(SHELL_MAPPING_PATH + ".*")));
    }
}

