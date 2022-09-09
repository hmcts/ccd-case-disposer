package uk.gov.hmcts.reform.ccd.constants;

public class TestConstants {

    public static final String GLOBAL_SEARCH_PATH = "/elastic-support/global-search/index";
    public static final String DOCUMENT_PATH = "/documents";
    public static final String ROLE_ASSIGNMENT_PATH = "/am/role-assignments";
    public static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorization";
    public static final String AUTHORISATION_HEADER = "Authorization";
    public static final String FILES_FOLDER = "files/";
    public static final String JSON_FOLDER = "/json/";
    public static final String DOCUMENT_STORE_USER = "case.disposer.idam.system.user@gmail.com";

    public static final String CASE_DATA_TTL = "classpath:casedata/case-data-ttl.json";
    public static final String JURISDICTION = "DISPOSER_MASTER";
    public static final String CREATE_CASE_EVENT = "createCaseTTL";
    public static final String UPDATE_CASE_EVENT = "updateCaseTTLInc";

    private TestConstants() {
    }
}
