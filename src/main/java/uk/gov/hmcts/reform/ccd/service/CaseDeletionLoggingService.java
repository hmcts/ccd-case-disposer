package uk.gov.hmcts.reform.ccd.service;


import com.google.common.collect.Lists;
import dnl.utils.text.table.TextTable;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;
import uk.gov.hmcts.reform.ccd.util.SummaryStringLogBuilder;
import uk.gov.hmcts.reform.ccd.util.log.CaseDataViewBuilder;
import uk.gov.hmcts.reform.ccd.util.log.TableTextBuilder;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.reform.ccd.util.LogConstants.DELETED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.FAILED_STATE;
import static uk.gov.hmcts.reform.ccd.util.LogConstants.SIMULATED_STATE;

@Named
@Slf4j
@RequiredArgsConstructor
public class CaseDeletionLoggingService {

    private final TableTextBuilder tableTextBuilder;
    private final CaseDataViewBuilder caseDataViewBuilder;
    private final ParameterResolver parameterResolver;
    private final SummaryStringLogBuilder summaryStringLogBuilder;
    private final ProcessedCasesRecordHolder processedCasesRecordHolder;


    public void logCases() {

        logSimulatedCases();

        final List<CaseDataView> caseDataViews = buildCaseDataViews();

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
    }

    private void logSimulatedCases() {
        Set<CaseData> simulatedCases = processedCasesRecordHolder.getSimulatedCases();
        log.info("Total Simulated Cases: " + simulatedCases.size());
        List<List<CaseData>> partitions = Lists.partition(new ArrayList<>(simulatedCases),
                                                          parameterResolver.getAppInsightsLogSize());

        for (List<CaseData> partition : partitions) {
            StringBuilder batchLog = new StringBuilder();
            partition.forEach(caseData -> {
                batchLog.append(String.format("Simulated case type: %s, Case ref: %s%n",
                                              caseData.getCaseType(), caseData.getReference()));
            });

            log.info(batchLog.toString());
        }
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
                .buildSummaryString(0, 0, 0, 0, 0, 0,
                                    Collections.emptyMap());
        log.info(summaryString);
    }

    private List<CaseDataView> buildCaseDataViews() {
        final List<CaseDataView> caseDataViews = new ArrayList<>();

        caseDataViewBuilder.buildCaseDataViewList(
            processedCasesRecordHolder.getSuccessfullyDeletedCases(), caseDataViews, DELETED_STATE);
        caseDataViewBuilder.buildCaseDataViewList(
            new ArrayList<>(processedCasesRecordHolder.getSimulatedCases()), caseDataViews, SIMULATED_STATE);
        caseDataViewBuilder.buildCaseDataViewList(
            processedCasesRecordHolder.getFailedToDeleteDeletedCases(), caseDataViews, FAILED_STATE);

        return caseDataViews;
    }
}
