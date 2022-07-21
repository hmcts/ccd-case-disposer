package uk.gov.hmcts.reform.ccd.service;


import com.google.common.collect.Lists;
import dnl.utils.text.table.TextTable;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.data.model.CaseDataView;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.SummaryStringLogBuilder;
import uk.gov.hmcts.reform.ccd.util.log.CaseDataViewBuilder;
import uk.gov.hmcts.reform.ccd.util.log.CaseDataViewHolder;
import uk.gov.hmcts.reform.ccd.util.log.TableTextBuilder;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@Slf4j
public class CaseDeletionSimulationService {

    private final TableTextBuilder tableTextBuilder;
    private final CaseDataViewBuilder caseDataViewBuilder;
    private final CaseDataViewHolder caseDataViewHolder;
    private final ParameterResolver parameterResolver;
    private final SummaryStringLogBuilder summaryStringLogBuilder;

    @Inject
    public CaseDeletionSimulationService(final TableTextBuilder tableTextBuilder,
                                         final CaseDataViewBuilder caseDataViewBuilder,
                                         final CaseDataViewHolder caseDataViewHolder,
                                         final ParameterResolver parameterResolver,
                                         final SummaryStringLogBuilder summaryStringLogBuilder) {
        this.tableTextBuilder = tableTextBuilder;
        this.caseDataViewBuilder = caseDataViewBuilder;
        this.caseDataViewHolder = caseDataViewHolder;
        this.parameterResolver = parameterResolver;
        this.summaryStringLogBuilder = summaryStringLogBuilder;
    }

    public void logCaseFamilies(final List<CaseFamily> deletedLinkedFamilies,
                                final List<CaseFamily> simulatedLinkedFamilies) {

        final List<CaseDataView> caseDataViews = buildCaseDataViews(deletedLinkedFamilies, simulatedLinkedFamilies);

        final List<List<CaseDataView>> caseViewPartition = Lists.partition(caseDataViews,
                parameterResolver.getAppInsightsLogSize());

        final AtomicInteger partCounter = new AtomicInteger(0);

        caseViewPartition.forEach(caseViewListPartition -> {
            final String summaryString = summaryStringLogBuilder.buildSummaryString(deletedLinkedFamilies,
                    simulatedLinkedFamilies,
                    partCounter.incrementAndGet(),
                    caseViewPartition.size());

            final TextTable textTable = tableTextBuilder.buildTextTable(caseViewListPartition);

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final PrintStream printStream = new PrintStream(outputStream, true);

            textTable.printTable(printStream, 0);

            log.info(summaryString.concat(outputStream.toString()));
        });

        caseDataViewHolder.setUpData(caseDataViews);
    }

    private List<CaseDataView> buildCaseDataViews(final List<CaseFamily> deletedLinkedFamilies,
                                                  final List<CaseFamily> simulatedLinkedFamilies) {
        final List<CaseDataView> caseDataViews = new ArrayList<>();

        caseDataViewBuilder.buildCaseDataViewList(deletedLinkedFamilies, caseDataViews, true);
        caseDataViewBuilder.buildCaseDataViewList(simulatedLinkedFamilies, caseDataViews, false);

        return caseDataViews;
    }
}