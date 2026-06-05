package uk.gov.hmcts.reform.ccd.util;

import jakarta.inject.Named;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SUMMARY_HEADING_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.TOTAL_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.TOTAL_STRING;

@Named
public class SummaryStringLogBuilder {

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy, HH:mm", Locale.UK);

    public String buildSummaryString(final List<CaseDataView> caseDataViews) {
        final int deletedCases = countCaseFamilies(caseDataViews, DELETED_STATE);
        final int simulatedCases = countCaseFamilies(caseDataViews, SIMULATED_STATE);
        final int failedCases = countCaseFamilies(caseDataViews, FAILED_STATE);
        final int totalCases = deletedCases + simulatedCases + failedCases;

        Map<String, Long> caseTypeAndStateCount = getCaseTypeAndStateCount(caseDataViews);

        return buildSummaryString(
            deletedCases,
            simulatedCases,
            failedCases,
            totalCases,
            caseTypeAndStateCount
        );
    }

    public String buildSummaryString(final int deleted,
                                     final int simulated,
                                     final int failed,
                                     final int total,
                                     final Map<String, Long> caseTypeAndStateCount) {

        final StringBuilder stringBuilder = new StringBuilder(SUMMARY_HEADING_STRING);
        stringBuilder.append(LocalDateTime.now(ZoneOffset.UTC).format(DATE_FORMATTER))
            .append(CR_STRING)
            .append(TOTAL_CASES_STRING).append(total)
            .append(DELETED_CASES_STRING).append(deleted)
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
