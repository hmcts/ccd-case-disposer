package uk.gov.hmcts.reform.ccd.util;

import jakarta.inject.Named;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.ccd.util.LogConstants.CR_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.FAILED_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.FAILED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SUMMARY_HEADING_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.TOTAL_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.TOTAL_STRING;

@Named
public class SummaryStringLogBuilder {

    public String buildSummaryString(final List<CaseDataView> caseDataViews,
                                     final int partCounter,
                                     final int totalSize,
                                     final List<String> deletedCaseTypes,
                                     final List<String> simulatedCaseTypes) {
        final int deletedCases = countCaseFamilies(caseDataViews, DELETED_STATE);
        final int simulatedCases = countCaseFamilies(caseDataViews, SIMULATED_STATE);
        final int failedCases = countCaseFamilies(caseDataViews, FAILED_STATE);
        final int totalCases = deletedCases + simulatedCases + failedCases;

        Map<String, Integer> deletedCaseTypeAndCounts = getCaseTypeAndCountByState(
            caseDataViews,DELETED_STATE,deletedCaseTypes);
        Map<String, Integer> simulatedCaseTypeAndCounts = getCaseTypeAndCountByState(
            caseDataViews,SIMULATED_STATE,simulatedCaseTypes);

        return buildSummaryString(
            deletedCases,
            simulatedCases,
            failedCases,
            totalCases,
            partCounter,
            totalSize,
            deletedCaseTypeAndCounts,
            simulatedCaseTypeAndCounts
        );
    }

    public String buildSummaryString(final int deleted,
                                     final int simulated,
                                     final int failed,
                                     final int total,
                                     final int partCounter,
                                     final int totalSize,
                                     final Map<String, Integer> deletedCaseTypeAndCounts,
                                     final Map<String, Integer> simulatedCaseTypeAndCounts) {

        final StringBuilder stringBuilder = new StringBuilder(String.format(SUMMARY_HEADING_STRING, partCounter,
                                                                            totalSize
        ));
        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMMM yyyy, HH:mm", Locale.UK);
        stringBuilder.append(dateFormat.format(new Date()))
            .append(CR_STRING)
            .append(TOTAL_CASES_STRING).append(total)
            .append(CR_STRING)
            .append(DELETED_CASES_STRING).append(deleted)
            .append(CR_STRING)
            .append(SIMULATED_CASES_STRING).append(simulated)
            .append(FAILED_CASES_STRING).append(failed)
            .append(CR_STRING);
        if (simulatedCaseTypeAndCounts != null && !simulatedCaseTypeAndCounts.isEmpty()) {
            simulatedCaseTypeAndCounts.forEach((caseType, count) ->
                   stringBuilder.append(TOTAL_STRING).append(caseType).append(" ")
                       .append(SIMULATED_CASES_STRING).append(count).append(CR_STRING)
            );
        }
        if (deletedCaseTypeAndCounts != null && !deletedCaseTypeAndCounts.isEmpty()) {
            deletedCaseTypeAndCounts.forEach((caseType, count) ->
                   stringBuilder.append(TOTAL_STRING).append(caseType).append(" ")
                    .append(DELETED_CASES_STRING).append(count).append(CR_STRING)
            );
        }
        return stringBuilder.toString();
    }

    private int countCaseFamilies(final List<CaseDataView> caseDataViews, final String state) {
        return caseDataViews.stream()
            .filter(caseDataView -> caseDataView.getState().equals(state))
            .collect(Collectors.toList()).size();
    }

    private int countCaseFamiliesByCaseTypeAndState(final List<CaseDataView> caseDataViews,
                                                    final String caseType, final String state) {
        return (int) caseDataViews.stream()
            .filter(caseDataView -> caseDataView.getCaseType() != null
                && caseDataView.getCaseType().equals(caseType)
                && caseDataView.getState().equals(state))
            .count();
    }

    private Map<String, Integer> getCaseTypeAndCountByState(final List<CaseDataView> caseDataViews,
                                                            String state, List<String> caseTypes) {

        Map<String, Integer> caseTypeAndCounts = new HashMap<>();
        for (String caseType : caseTypes) {
            int caseTypeCount = countCaseFamiliesByCaseTypeAndState(caseDataViews, caseType, state);
            caseTypeAndCounts.put(caseType, caseTypeCount);
        }
        return caseTypeAndCounts;
    }

}
