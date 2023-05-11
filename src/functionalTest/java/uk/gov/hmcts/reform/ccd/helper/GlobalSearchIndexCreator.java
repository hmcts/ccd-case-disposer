package uk.gov.hmcts.reform.ccd.helper;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.GLOBAL_SEARCH_PATH;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.SERVICE_AUTHORISATION_HEADER;

@Service
public class GlobalSearchIndexCreator {

    @Value("${create.global.search}")
    private String createGlobalSearch;

    @Inject
    private ParameterResolver parameterResolver;

    @Autowired
    private SecurityUtils securityUtils;

    public void createGlobalSearchIndex() {
        if (createGlobalSearch.equals("true") && isPreview()) {
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
    }

    private boolean isPreview() {
        final Optional<String> env = Optional.ofNullable(System.getenv("ENV"));
        if (env.isPresent() && env.get().equals("preview")) {
            return true;
        }
        return false;
    }
}
