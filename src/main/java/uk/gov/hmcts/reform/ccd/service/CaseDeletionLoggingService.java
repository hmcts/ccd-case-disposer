package uk.gov.hmcts.reform.ccd.service;


import com.google.common.collect.Lists;
import dnl.utils.text.table.TextTable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.SummaryStringLogBuilder;
import uk.gov.hmcts.reform.ccd.util.log.CaseDataViewBuilder;
import uk.gov.hmcts.reform.ccd.util.log.SimulatedCaseDataViewHolder;
import uk.gov.hmcts.reform.ccd.util.log.TableTextBuilder;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.FAILED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_STATE;

@Named
@Slf4j
public class CaseDeletionLoggingService {

    private final TableTextBuilder tableTextBuilder;
    private final CaseDataViewBuilder caseDataViewBuilder;
    private final SimulatedCaseDataViewHolder simulatedCaseDataViewHolder;
    private final ParameterResolver parameterResolver;
    private final SummaryStringLogBuilder summaryStringLogBuilder;

    @Inject
    public CaseDeletionLoggingService(final TableTextBuilder tableTextBuilder,
                                      final CaseDataViewBuilder caseDataViewBuilder,
                                      final SimulatedCaseDataViewHolder simulatedCaseDataViewHolder,
                                      final ParameterResolver parameterResolver,
                                      final SummaryStringLogBuilder summaryStringLogBuilder) {
        this.tableTextBuilder = tableTextBuilder;
        this.caseDataViewBuilder = caseDataViewBuilder;
        this.simulatedCaseDataViewHolder = simulatedCaseDataViewHolder;
        this.parameterResolver = parameterResolver;
        this.summaryStringLogBuilder = summaryStringLogBuilder;
    }

    public void logCaseFamilies(final List<CaseFamily> deletedLinkedFamilies,
                                final List<CaseFamily> simulatedLinkedFamilies,
                                final List<CaseFamily> failedLinkedFamilies) {

        final List<CaseDataView> caseDataViews = buildCaseDataViews(deletedLinkedFamilies,
                simulatedLinkedFamilies,
                failedLinkedFamilies);

        final List<List<CaseDataView>> caseViewPartition = Lists.partition(caseDataViews,
                parameterResolver.getAppInsightsLogSize());

        if (!caseViewPartition.isEmpty()) {
            final AtomicInteger partCounter = new AtomicInteger(0);

            caseViewPartition.forEach(caseViewListPartition -> {
                final String summaryString = summaryStringLogBuilder.buildSummaryString(caseDataViews,
                        partCounter.incrementAndGet(),
                        caseViewPartition.size());

                final ByteArrayOutputStream outputStream = buildTextTable(caseViewListPartition);

                log.info(summaryString.concat(outputStream.toString()));
            });
        } else {
            logDataIfNoDeletableOrSimulatedCasesFound();
        }
        simulatedCaseDataViewHolder.setUpData(caseDataViews);
    }

    private ByteArrayOutputStream buildTextTable(final List<CaseDataView> caseViewListPartition) {
        final TextTable textTable = tableTextBuilder.buildTextTable(caseViewListPartition);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(outputStream, true);

        textTable.printTable(printStream, 0);
        return outputStream;
    }

    private void logDataIfNoDeletableOrSimulatedCasesFound() {
        final String summaryString = summaryStringLogBuilder
                .buildSummaryString(0, 0,0, 0, 0, 0);
        log.info(summaryString);
    }

    private List<CaseDataView> buildCaseDataViews(final List<CaseFamily> deletedLinkedFamilies,
                                                  final List<CaseFamily> simulatedLinkedFamilies,
                                                  final List<CaseFamily> failedLinkedFamilies) {
        final List<CaseDataView> caseDataViews = new ArrayList<>();

        caseDataViewBuilder.buildCaseDataViewList(deletedLinkedFamilies, caseDataViews, DELETED_STATE);
        caseDataViewBuilder.buildCaseDataViewList(simulatedLinkedFamilies, caseDataViews, SIMULATED_STATE);
        caseDataViewBuilder.buildCaseDataViewList(failedLinkedFamilies, caseDataViews, FAILED_STATE);

        removeCaseDataViewDuplicates(caseDataViews);

        return caseDataViews;
    }

    private void removeCaseDataViewDuplicates(final List<CaseDataView> caseDataViews) {
        final Set<Long> nameSet = new HashSet<>();
        caseDataViews.removeIf(e -> (!nameSet.add(e.getCaseRef())));
    }
}
