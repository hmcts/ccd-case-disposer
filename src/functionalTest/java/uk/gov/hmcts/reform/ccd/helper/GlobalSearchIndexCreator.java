package uk.gov.hmcts.reform.ccd.helper;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
public class GlobalSearchIndexCreator {

    private static final String GLOBAL_SEARCH_PATH = "/elastic-support/global-search/index";
    private static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorization";
    private static final String AUTHORISATION_HEADER = "Authorization";

    @Inject
    private ParameterResolver parameterResolver;

    @Autowired
    private SecurityUtils securityUtils;

    public void createGlobalSearchIndex() {
        final Response response = RestAssured
                .given()
                .relaxedHTTPSValidation()
                .baseUri(parameterResolver.getCaseDefinitionHost().concat(GLOBAL_SEARCH_PATH))
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .header(SERVICE_AUTHORISATION_HEADER, securityUtils.getServiceAuthorization())
                .header(AUTHORISATION_HEADER, securityUtils.getIdamClientToken())
                .when()
                .post()
                .andReturn();

        assertThat(response.getStatusCode()).isEqualTo(201);
    }

    public void testEsConnection() {
        final Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri("http://es-ccd-case-disposer-pr-47.service.core-compute-preview.internal/_aliases?pretty=true")
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .when()
            .get()
            .andReturn();

        assertThat(response.getStatusCode()).isEqualTo(200);
    }
}
