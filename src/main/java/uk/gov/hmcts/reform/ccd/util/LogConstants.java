package uk.gov.hmcts.reform.ccd.util;

import java.util.List;

import static java.util.Arrays.asList;

public class LogConstants {
    public static final String SUMMARY_HEADING_STRING = "\r\n\r\nCase Disposer Deletion Summary: ";
    public static final String TOTAL_CASES_STRING = "\r\nTotal cases : ";
    public static final String DELETED_CASES_STRING = "\r\nDeleted cases : ";
    public static final String NOT_DELETED_CASES_STRING = "\r\nNot deleted cases : ";
    public static final String SIMULATED_CASES_STRING = "\r\nSimulated cases : ";
    public static final String FAILED_CASES_STRING = "\r\nFailed cases : ";
    public static final String CR_STRING = "\r\n";
    public static final String TOTAL_STRING = "\r\nTotal ";
    public static final String CASES_STRING = " cases : ";
    public static final List<String> COLUMN_NAMES = asList("Case Type", "Case ID", "Delete State");
    public static final String DELETED_STATE = "DELETED";
    public static final String SIMULATED_STATE = "SIMULATED";
    public static final String FAILED_STATE = "FAILED";
    public static final String NOT_DELETED_STATE = "NOT DELETED";

    private LogConstants() {
    }
}
