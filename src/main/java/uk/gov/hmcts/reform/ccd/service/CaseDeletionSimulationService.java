package uk.gov.hmcts.reform.ccd.service;


import dnl.utils.text.table.TextTable;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.util.log.CaseDataViewBuilder;
import uk.gov.hmcts.reform.ccd.util.log.CaseDataViewHolder;
import uk.gov.hmcts.reform.ccd.util.log.TableTextBuilder;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.inject.Named;

import static uk.gov.hmcts.reform.ccd.util.LogConstants.CR_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_CASES_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SUMMARY_HEADING_STRING;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.TOTAL_CASES_STRING;

@Named
@Slf4j
public class CaseDeletionSimulationService {

    private final TableTextBuilder tableTextBuilder;
    private final CaseDataViewBuilder caseDataViewBuilder;
    private final CaseDataViewHolder caseDataViewHolder;

    @Inject
    public CaseDeletionSimulationService(final TableTextBuilder tableTextBuilder,
                                         final CaseDataViewBuilder caseDataViewBuilder,
                                         final CaseDataViewHolder caseDataViewHolder) {
        this.tableTextBuilder = tableTextBuilder;
        this.caseDataViewBuilder = caseDataViewBuilder;
        this.caseDataViewHolder = caseDataViewHolder;
    }

    public void logCaseFamilies(final List<CaseFamily> deletedLinkedFamilies,
                                final List<CaseFamily> simulatedLinkedFamilies) {

        final String summaryString = buildSummaryString(deletedLinkedFamilies, simulatedLinkedFamilies);
        final List<CaseDataView> caseDataViews = buildCaseDataViews(deletedLinkedFamilies, simulatedLinkedFamilies);

        final TextTable textTable = tableTextBuilder.buildTextTable(caseDataViews);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(outputStream, true);

        caseDataViewHolder.setUpData(caseDataViews);

        textTable.printTable(printStream, 0);

        log.info(summaryString.concat(outputStream.toString()));
    }


    public String buildSummaryString(final List<CaseFamily> deletedLinkedFamilies,
                                     final List<CaseFamily> simulatedLinkedFamilies) {
        final int deletedCases = countCaseFamilies(deletedLinkedFamilies);
        final int simulatedCases = countCaseFamilies(simulatedLinkedFamilies);
        final int totalCases = deletedCases + simulatedCases;

        return getSummaryString(deletedCases, simulatedCases, totalCases);
    }

    private List<CaseDataView> buildCaseDataViews(final List<CaseFamily> deletedLinkedFamilies,
                                                  final List<CaseFamily> simulatedLinkedFamilies) {
        final List<CaseDataView> caseDataViews = new ArrayList<>();

        caseDataViewBuilder.buildCaseDataViewList(deletedLinkedFamilies, caseDataViews, true);
        caseDataViewBuilder.buildCaseDataViewList(simulatedLinkedFamilies, caseDataViews, false);

        return caseDataViews;
    }

    private String getSummaryString(final int deleted,
                                    final int simulated,
                                    final int total) {
        final StringBuilder stringBuilder = new StringBuilder(SUMMARY_HEADING_STRING);
        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMMM yyyy", Locale.UK);
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