package uk.gov.hmcts.reform.ccd.util;

@SuppressWarnings("ALL")
public class RestConstants {
    private RestConstants() {
    }

    public static final String DELETE_DOCUMENT_PATH = "/documents/delete";
    public static final String DELETE_ROLE_PATH = "/am/role-assignments/query/delete";
    public static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorization";
    public static final String AUTHORISATION_HEADER = "Authorization";
}
