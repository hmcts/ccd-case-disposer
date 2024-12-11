package uk.gov.hmcts.reform.ccd;

import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionLoggingService;

import uk.gov.hmcts.reform.ccd.service.CaseDeletionService;
import uk.gov.hmcts.reform.ccd.service.CaseFinderService;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;
import uk.gov.hmcts.reform.ccd.util.log.CaseFamiliesFilter;


import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.ccd.util.CaseFamilyUtil.getCaseData;

@Slf4j
@Named
@RequiredArgsConstructor
public class ApplicationExecutor {
    private final CaseFinderService caseFindingService;
    private final CaseDeletionService caseDeletionService;
    private final CaseFamiliesFilter caseFamiliesFilter;
    private final ParameterResolver parameterResolver;
    private final ProcessedCasesRecordHolder processedCasesRecordHolder;
    private final CaseDeletionLoggingService caseDeletionLoggingService;

    public void execute() {
        log.info("Case-Disposer started...");
        final List<CaseFamily> caseFamiliesDueDeletion = caseFindingService.findCasesDueDeletion();
        final List<CaseFamily> deletableCasesOnly = caseFamiliesFilter.getDeletableCasesOnly(caseFamiliesDueDeletion);
        final List<CaseFamily> deletableLinkedFamiliesSimulation = caseFamiliesFilter.geSimulationCasesOnly(
            caseFamiliesDueDeletion);

        Integer requestLimit = parameterResolver.getRequestLimit();

        final Set<CaseData> allDeletableCases = getCaseData(deletableCasesOnly);
        final Set<CaseData> simulatedCases = getCaseData(deletableLinkedFamiliesSimulation);

        processedCasesRecordHolder.setSimulatedCases(simulatedCases);

        processCases(allDeletableCases, requestLimit);

        caseDeletionLoggingService.logCases();

        log.info("Case-Disposer finished.");
    }


    private void processCases(final Set<CaseData> cases, int requestLimit) {
        for (CaseData caseData : cases) {
            if (requestLimit == 0) {
                break;
            }
            caseDeletionService.deleteCaseData(caseData);
            requestLimit--;
            processedCasesRecordHolder.addProcessedCase(caseData);

        }
    }
}
