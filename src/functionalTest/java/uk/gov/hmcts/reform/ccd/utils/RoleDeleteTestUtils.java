package uk.gov.hmcts.reform.ccd.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.exception.CaseDisposerFunctionalTestException;
import uk.gov.hmcts.reform.ccd.helper.S2SHelper;
import uk.gov.hmcts.reform.ccd.helper.SecurityUtils;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.DataStoreRecordHolder;
import uk.gov.hmcts.reform.ccd.util.log.RoleDeletionRecordHolder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    private DataStoreRecordHolder dataStoreRecordHolder;

    @Inject
    private SecurityUtils securityUtils;

    @Inject
    private ParameterResolver parameterResolver;

    @Inject
    private S2SHelper s2SHelper;

    @Inject
    private FileUtils fileUtils;

    public void verifyRoleAssignmentDeletion(final Map<String, String> deletableRoles) {
        deletableRoles.entrySet().forEach(entry -> {

            final List<String> deletedCaseIdList = dataStoreRecordHolder.getDatastoreCases().get(entry.getKey());

            deletedCaseIdList.forEach(caseId -> {
                final int caseRolesDeletionActualResults = roleDeletionRecordHolder
                        .getCaseRolesDeletionResults(caseId);

                assertThat(caseRolesDeletionActualResults).isEqualTo(HttpStatus.OK.value());
            });
        });
    }

    public void createRoleAssignment(final Map<String, String> deletableRoles) {
        deletableRoles.entrySet().forEach(entry -> dataStoreRecordHolder.getDatastoreCases().get(entry.getKey())
                .forEach(caseId -> {
                    final Response response = RestAssured
                            .given()
                            .relaxedHTTPSValidation()
                            .baseUri(parameterResolver.getRoleAssignmentsHost() + ROLE_ASSIGNMENT_PATH)
                            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                            .header(SERVICE_AUTHORISATION_HEADER, s2SHelper.getToken())
                            .header(AUTHORISATION_HEADER, securityUtils.getIdamClientToken())
                            .body(createJsonString(entry.getValue(), caseId))
                            .when()
                            .post()
                            .andReturn();

                    assertThat(response.getStatusCode()).isEqualTo(201);
                })
        );
    }

    private String createJsonString(final String fileName,
                                    final String caseId) {
        try {
            final String jsonFromFile = fileUtils.getJsonFromFile(fileName);

            final JSONObject jsonObject = new JSONObject(jsonFromFile);
            final JSONArray jsonObjectForState = jsonObject.getJSONArray("requestedRoles");
            final JSONObject attributes = jsonObjectForState.getJSONObject(0).getJSONObject("attributes");

            attributes.put("caseId", caseId);
            return jsonObject.toString();
        } catch (IOException | JSONException e) {
            throw new CaseDisposerFunctionalTestException("Unable to create role assignment json string", e);
        }


    }

}
