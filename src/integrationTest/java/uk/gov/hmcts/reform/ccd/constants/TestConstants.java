package uk.gov.hmcts.reform.ccd.constants;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;

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
}
