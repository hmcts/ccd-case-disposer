package uk.gov.hmcts.reform.ccd.constants;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;

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

}
