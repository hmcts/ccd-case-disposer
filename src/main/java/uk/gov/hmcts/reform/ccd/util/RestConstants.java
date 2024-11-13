package uk.gov.hmcts.reform.ccd.util;

@SuppressWarnings({"java:S1075"})
public class RestConstants {
    private RestConstants() {
    }

    // Paths
    public static final String DELETE_DOCUMENT_PATH = "/documents/delete";
    public static final String DELETE_ROLE_PATH = "/am/role-assignments/query/delete";
    public static final String LAU_SAVE_PATH = "/audit/caseAction";
    public static final String DELETE_TASKS_PATH = "/task/delete";
    public static final String DELETE_HEARINGS_PATH = "/delete";

    // Headers
    public static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorization";
    public static final String AUTHORISATION_HEADER = "Authorization";

    public static final String MEDIA_TYPE_POST_DELETE_ASSIGNMENTS =
        "application/vnd.uk.gov.hmcts.role-assignment-service"
        + ".post-assignments-delete-request+json;charset=UTF-8;version=1.0";


    public static final String HEARING_RECORDINGS_CASE_TYPE = "HearingRecordings";
}
