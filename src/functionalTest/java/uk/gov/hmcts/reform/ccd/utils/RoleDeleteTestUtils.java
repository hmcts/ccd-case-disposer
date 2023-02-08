package uk.gov.hmcts.reform.ccd.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.helper.S2SHelper;
import uk.gov.hmcts.reform.ccd.helper.SecurityUtils;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.RoleDeletionRecordHolder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.with;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.ROLE_ASSIGNMENT_PATH;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.SERVICE_AUTHORISATION_HEADER;

@Component
public class RoleDeleteTestUtils {

    @Inject
    private RoleDeletionRecordHolder roleDeletionRecordHolder;

    @Inject
    private SecurityUtils securityUtils;

    @Inject
    private ParameterResolver parameterResolver;

    @Inject
    private S2SHelper s2SHelper;

    @Inject
    private FileUtils fileUtils;

    public void verifyRoleAssignmentDeletion(final Map<Long, List<String>> deletableRoles) {
        deletableRoles.entrySet().forEach(entry -> {
            with().await()
                    .untilAsserted(() -> {
                        final int caseRolesDeletionActualResults = roleDeletionRecordHolder
                                .getCaseRolesDeletionResults(Long.toString(entry.getKey()));

                        assertThat(caseRolesDeletionActualResults).isEqualTo(HttpStatus.OK.value());
                    });
        });
    }

    public void createRoleAssignment(final Map<Long, List<String>> deletableRoles) {

        deletableRoles.entrySet()
                .forEach(entry -> entry.getValue()
                        .forEach(fileName -> {
                            try {
                                final Response response = RestAssured
                                        .given()
                                        .relaxedHTTPSValidation()
                                        .baseUri(parameterResolver.getRoleAssignmentsHost() + ROLE_ASSIGNMENT_PATH)
                                        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                        .header(SERVICE_AUTHORISATION_HEADER, s2SHelper.getToken())
                                        .header(AUTHORISATION_HEADER, securityUtils.getIdamClientToken())
                                        .body(fileUtils.getJsonFromFile(fileName))
                                        .when()
                                        .post()
                                        .andReturn();

                                assertThat(response.getStatusCode()).isEqualTo(201);

                            } catch (final IOException e) {
                                e.printStackTrace();
                            }
                        }));
    }

}
