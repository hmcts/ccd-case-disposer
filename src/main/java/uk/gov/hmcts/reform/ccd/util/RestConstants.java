package uk.gov.hmcts.reform.ccd.util;

@SuppressWarnings("ALL")
public class RestConstants {
    private RestConstants() {
    }

    public static final String DELETE_DOCUMENT_PATH = "/documents/delete";
    public static final String DELETE_ROLE_PATH = "/am/role-assignments/query/delete";
    public static final String QUERY_ROLE_PATH = "/am/role-assignments/query";
    public static final String LAU_SAVE_PATH = "/audit/caseAction";

    public static final String DELETE_TASKS_PATH = "/task-management/tasks";


    public static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorization";
    public static final String AUTHORISATION_HEADER = "Authorization";
    public static final String MEDIATYPE_ROLE_FETCH =
        "application/vnd.uk.gov.hmcts.role-assignment-service.post-assignment-query-request+json;"
            + "charset=UTF-8;version=2.0";
}
