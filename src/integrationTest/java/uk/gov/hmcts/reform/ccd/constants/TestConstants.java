package uk.gov.hmcts.reform.ccd.constants;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.data.lau.ActionLog;
import uk.gov.hmcts.reform.ccd.data.lau.CaseActionPostRequestResponse;

import java.util.Map;

public class TestConstants {
    private TestConstants() {
    }

    public static Map<String, CaseDocumentsDeletionResults> DOCUMENT_DELETE = Map.ofEntries(
            Map.entry("1504259907353529", new CaseDocumentsDeletionResults(1, 1)),
            Map.entry("1504259907353528", new CaseDocumentsDeletionResults(2, 2)),
            Map.entry("1504259907353526", new CaseDocumentsDeletionResults(3, 3)),
            Map.entry("1504259907353525", new CaseDocumentsDeletionResults(4, 4)),
            Map.entry("1504259907353524", new CaseDocumentsDeletionResults(5, 5)),
            Map.entry("1504259907353523", new CaseDocumentsDeletionResults(6, 6)),
            Map.entry("1504259907353522", new CaseDocumentsDeletionResults(7, 7)),
            Map.entry("1504259907353519", new CaseDocumentsDeletionResults(8, 8)),
            Map.entry("1504259907353518", new CaseDocumentsDeletionResults(9, 9)),
            Map.entry("1504259907353527", new CaseDocumentsDeletionResults(10, 10)),
            Map.entry("1504259907351111", new CaseDocumentsDeletionResults(11, 11)),
            Map.entry("1504259907350000", new CaseDocumentsDeletionResults(null, null))
    );

    public static Map<String, Integer> ROLE_DELETE = Map.ofEntries(
            Map.entry("1504259907353529", HttpStatus.OK.value()),
            Map.entry("1504259907353528", HttpStatus.OK.value()),
            Map.entry("1504259907353526", HttpStatus.OK.value()),
            Map.entry("1504259907353525", HttpStatus.OK.value()),
            Map.entry("1504259907353524", HttpStatus.OK.value()),
            Map.entry("1504259907353523", HttpStatus.OK.value()),
            Map.entry("1504259907353522", HttpStatus.OK.value()),
            Map.entry("1504259907353519", HttpStatus.OK.value()),
            Map.entry("1504259907353518", HttpStatus.OK.value()),
            Map.entry("1504259907353527", HttpStatus.OK.value()),
            Map.entry("1504259907351111", HttpStatus.OK.value()),
            Map.entry("1504259907350000", HttpStatus.OK.value())
    );

    public static Map<String, Integer> ROLE_QUERY = Map.ofEntries(
        Map.entry("1504259907353529", HttpStatus.OK.value()),
        Map.entry("1504259907353528", HttpStatus.OK.value()),
        Map.entry("1504259907353526", HttpStatus.OK.value()),
        Map.entry("1504259907353525", HttpStatus.OK.value()),
        Map.entry("1504259907353524", HttpStatus.OK.value()),
        Map.entry("1504259907353523", HttpStatus.OK.value()),
        Map.entry("1504259907353522", HttpStatus.OK.value()),
        Map.entry("1504259907353519", HttpStatus.OK.value()),
        Map.entry("1504259907353518", HttpStatus.OK.value()),
        Map.entry("1504259907353527", HttpStatus.OK.value()),
        Map.entry("1504259907351111", HttpStatus.OK.value()),
        Map.entry("1504259907350000", HttpStatus.OK.value())
    );
    public static Map<String, CaseActionPostRequestResponse> LAU_QUERY = Map.ofEntries(
            Map.entry("1504259907353529", buildCaseActionPostRequest("1504259907353529")),
            Map.entry("1504259907353528", buildCaseActionPostRequest("1504259907353528")),
            Map.entry("1504259907353526", buildCaseActionPostRequest("1504259907353526")),
            Map.entry("1504259907353525", buildCaseActionPostRequest("1504259907353525")),
            Map.entry("1504259907353524", buildCaseActionPostRequest("1504259907353524")),
            Map.entry("1504259907353523", buildCaseActionPostRequest("1504259907353523")),
            Map.entry("1504259907353522", buildCaseActionPostRequest("1504259907353522")),
            Map.entry("1504259907353519", buildCaseActionPostRequest("1504259907353519")),
            Map.entry("1504259907353518", buildCaseActionPostRequest("1504259907353518")),
            Map.entry("1504259907353527", buildCaseActionPostRequest("1504259907353527")),
            Map.entry("1504259907351111", buildCaseActionPostRequest("1504259907351111")),
            Map.entry("1504259907350000", buildCaseActionPostRequest("1504259907350000"))
    );

    public static Map<String, Integer> TASKS_DELETE = Map.ofEntries(
            Map.entry("1504259907353529", HttpStatus.OK.value()),
            Map.entry("1504259907353528", HttpStatus.OK.value()),
            Map.entry("1504259907353526", HttpStatus.OK.value()),
            Map.entry("1504259907353525", HttpStatus.OK.value()),
            Map.entry("1504259907353524", HttpStatus.OK.value()),
            Map.entry("1504259907353523", HttpStatus.OK.value()),
            Map.entry("1504259907353522", HttpStatus.OK.value()),
            Map.entry("1504259907353519", HttpStatus.OK.value()),
            Map.entry("1504259907353518", HttpStatus.OK.value()),
            Map.entry("1504259907353527", HttpStatus.OK.value()),
            Map.entry("1504259907351111", HttpStatus.OK.value()),
            Map.entry("1504259907350000", HttpStatus.OK.value())
    );



    private static CaseActionPostRequestResponse buildCaseActionPostRequest(final String caseRef) {
        return new CaseActionPostRequestResponse(ActionLog.builder()
                .userId("123")
                .caseAction("DELETE")
                .caseTypeId("FT_MasterCaseType")
                .caseRef(caseRef)
                .caseJurisdictionId("BEFTA_MASTER")
                .timestamp("2021-08-23T22:20:05.023Z")
                .build());
    }
}
