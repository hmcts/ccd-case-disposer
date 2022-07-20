package uk.gov.hmcts.reform.ccd.util;

import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Named;

import static uk.gov.hmcts.reform.ccd.util.LogConstants.CR_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SUMMARY_HEADING_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.TOTAL_CASES_STRING;

@Named
public class SummaryStringLogBuilder {

    public String buildSummaryString(final List<CaseFamily> deletedLinkedFamilies,
                                     final List<CaseFamily> simulatedLinkedFamilies,
                                     final int partCounter,
                                     final int totalSize) {
        final int deletedCases = countCaseFamilies(deletedLinkedFamilies);
        final int simulatedCases = countCaseFamilies(simulatedLinkedFamilies);
        final int totalCases = deletedCases + simulatedCases;

        return getSummaryString(deletedCases, simulatedCases, totalCases, partCounter, totalSize);
    }

    private String getSummaryString(final int deleted,
                                    final int simulated,
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
                .append(CR_STRING);
        return stringBuilder.toString();
    }

    private int countCaseFamilies(final List<CaseFamily> caseFamilies) {
        final AtomicInteger atomicInteger = new AtomicInteger();
        //Add 1 for root cases
        caseFamilies.forEach(caseFamily -> atomicInteger.addAndGet(caseFamily.getLinkedCases().size() + 1));

        return atomicInteger.get();
    }
}
