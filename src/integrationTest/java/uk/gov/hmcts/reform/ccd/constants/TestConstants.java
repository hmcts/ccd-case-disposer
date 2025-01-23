package uk.gov.hmcts.reform.ccd.constants;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.data.lau.ActionLog;
import uk.gov.hmcts.reform.ccd.data.lau.CaseActionPostRequestResponse;

import java.util.Map;

public class TestConstants {
    private TestConstants() {
    }

    // Put special cases here only.
    public static final Map<String, CaseDocumentsDeletionResults> DOCUMENT_DELETE = Map.ofEntries(
        Map.entry("1504259907350000", new CaseDocumentsDeletionResults(null, null))
    );

    // Put only responses that are not 200. Any requests with case refs not specified here will return 200.
    public static final Map<String, Integer> ROLE_DELETE = Map.ofEntries(
        Map.entry("1504259907445512", HttpStatus.NOT_FOUND.value())
    );

    // Put only responses that are not 200. Any requests with case refs not specified here will return 200.
    // Example: Map.entry("1504259907351193", HttpStatus.BAD_REQUEST.value())
    // Though for this particular api call, doesn't look like response matter.
    public static final Map<String, Integer> ROLE_QUERY = Map.ofEntries(
    );

    // Put only responses that are not 201. Any requests with case refs not specified here will return 201.
    public static final Map<String, Integer> TASKS_DELETE = Map.ofEntries(
        Map.entry("1504259907351193", HttpStatus.BAD_GATEWAY.value())
    );

    // Put only responses that are not 204. Any requests with case refs not specified here will return 204.
    public static final Map<String, Integer> HEARINGS_DELETE = Map.ofEntries(
        Map.entry("1504259907445514", HttpStatus.NOT_FOUND.value())
    );

    public static Map<String, CaseActionPostRequestResponse> LAU_QUERY = Map.ofEntries(
        Map.entry("1504259907351163", buildCaseActionPostRequest("1504259907351163"))
    );

    private static CaseActionPostRequestResponse buildCaseActionPostRequest(final String caseRef) {
        return new CaseActionPostRequestResponse(ActionLog.builder()
                                                     .userId(null)
                                                     .caseAction("DELETE")
                                                     .caseTypeId("FT_MasterCaseType")
                                                     .caseRef(caseRef)
                                                     .caseJurisdictionId("BEFTA_MASTER")
                                                     .timestamp("2021-08-23T22:20:05.023Z")
                                                     .build());
    }


}
