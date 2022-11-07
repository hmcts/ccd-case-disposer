package uk.gov.hmcts.reform.ccd.util;

import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.inject.Named;

import static uk.gov.hmcts.reform.ccd.util.LogConstants.CR_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.FAILED_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.FAILED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SUMMARY_HEADING_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.TOTAL_CASES_STRING;

@Named
public class SummaryStringLogBuilder {

    public String buildSummaryString(final List<CaseDataView> caseDataViews,
                                     final int partCounter,
                                     final int totalSize) {
        final int deletedCases = countCaseFamilies(caseDataViews, DELETED_STATE);
        final int simulatedCases = countCaseFamilies(caseDataViews,SIMULATED_STATE);
        final int failedCases = countCaseFamilies(caseDataViews,FAILED_STATE);
        final int totalCases = deletedCases + simulatedCases + failedCases;

        return buildSummaryString(deletedCases, simulatedCases, failedCases, totalCases, partCounter, totalSize);
    }

    public String buildSummaryString(final int deleted,
                                     final int simulated,
                                     final int failed,
                                     final int total,
                                     final int partCounter,
                                     final int totalSize) {

        final StringBuilder stringBuilder = new StringBuilder(String.format(SUMMARY_HEADING_STRING, partCounter,
                totalSize));
        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMMM yyyy, HH:mm", Locale.UK);
        stringBuilder.append(dateFormat.format(new Date()))
                .append(CR_STRING)
                .append(TOTAL_CASES_STRING).append(total)
                .append(DELETED_CASES_STRING).append(deleted)
                .append(SIMULATED_CASES_STRING).append(simulated)
                .append(FAILED_CASES_STRING).append(failed)
                .append(CR_STRING);
        return stringBuilder.toString();
    }

    private int countCaseFamilies(final List<CaseDataView> caseDataViews, final String state) {
        return caseDataViews.stream()
                .filter(caseDataView -> caseDataView.getState().equals(state))
                .collect(Collectors.toList()).size();
    }
}
