package uk.gov.hmcts.reform.ccd.util;

import java.util.List;

import static java.util.Arrays.asList;

public class LogConstants {
    public static final String SUMMARY_HEADING_STRING = "\r\nCase Disposer Deletion Summary %s of %s : ";
    public static final String TOTAL_CASES_STRING = "\r\nTotal cases : ";
    public static final String DELETED_CASES_STRING = "\r\nDeleted cases : ";
    public static final String SIMULATED_CASES_STRING = "\r\nSimulated cases : ";
    public static final String CR_STRING = "\r\n";
    public static final List<String> COLUMN_NAMES = asList("Case Type", "Case ID", "Delete State", "Linked Case IDs");
    public static final String DELETED_STATE = "DELETED";
    public static final String SIMULATED_STATE = "SIMULATED";

    private LogConstants() {
    }
}
