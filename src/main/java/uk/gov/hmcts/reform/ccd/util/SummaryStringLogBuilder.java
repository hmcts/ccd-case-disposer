package uk.gov.hmcts.reform.ccd.util;

import jakarta.inject.Named;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.ccd.util.LogConstants.CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.CR_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.FAILED_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.FAILED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.NOT_DELETED_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.NOT_DELETED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SUMMARY_HEADING_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.TOTAL_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.TOTAL_STRING;

@Named
public class SummaryStringLogBuilder {

    public String buildSummaryString(final List<CaseDataView> caseDataViews) {
        final int deletedCases = countCaseFamilies(caseDataViews, DELETED_STATE);
        final int simulatedCases = countCaseFamilies(caseDataViews, SIMULATED_STATE);
        final int notDeleted = countCaseFamilies(caseDataViews, NOT_DELETED_STATE);
        final int failedCases = countCaseFamilies(caseDataViews, FAILED_STATE);
        final int totalCases = deletedCases + simulatedCases + failedCases;

        Map<String, Long> caseTypeAndStateCount = getCaseTypeAndStateCount(caseDataViews);

        return buildSummaryString(
            deletedCases,
            simulatedCases,
            notDeleted,
            failedCases,
            totalCases,
            caseTypeAndStateCount
        );
    }

    public String buildSummaryString(final int deleted,
                                     final int simulated,
                                     final int notDeleted,
                                     final int failed,
                                     final int total,
                                     final Map<String, Long> caseTypeAndStateCount) {

        final StringBuilder stringBuilder = new StringBuilder(SUMMARY_HEADING_STRING);
        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMMM yyyy, HH:mm", Locale.UK);
        stringBuilder.append(dateFormat.format(new Date()))
            .append(CR_STRING)
            .append(TOTAL_CASES_STRING).append(total)
            .append(DELETED_CASES_STRING).append(deleted)
            .append(NOT_DELETED_CASES_STRING).append(notDeleted)
            .append(SIMULATED_CASES_STRING).append(simulated)
            .append(FAILED_CASES_STRING).append(failed)
            .append(CR_STRING);

        caseTypeAndStateCount.forEach((caseTypeAndState, count) ->
            stringBuilder.append(TOTAL_STRING).append(caseTypeAndState).append(CASES_STRING)
                .append(count)
        );
        stringBuilder.append(CR_STRING);
        return stringBuilder.toString();
    }

    private int countCaseFamilies(final List<CaseDataView> caseDataViews, final String state) {
        return caseDataViews.stream()
            .filter(caseDataView -> caseDataView.getState().equals(state))
            .toList().size();
    }

    private Map<String, Long> getCaseTypeAndStateCount(final List<CaseDataView> caseDataViews) {
        return caseDataViews.stream()
            .collect(Collectors.groupingBy(
                caseDataView -> caseDataView.getCaseType() + " " + caseDataView.getState(),
                Collectors.counting()
            ));
    }

}
