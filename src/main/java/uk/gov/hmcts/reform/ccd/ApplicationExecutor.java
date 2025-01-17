package uk.gov.hmcts.reform.ccd;

import com.google.common.base.Stopwatch;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.exception.LogAndAuditException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionLoggingService;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionService;
import uk.gov.hmcts.reform.ccd.service.CaseFinderService;
import uk.gov.hmcts.reform.ccd.service.v2.CaseCollectorService;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;
import uk.gov.hmcts.reform.ccd.util.log.CaseFamiliesFilter;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final CaseCollectorService caseCollectorService;
    private final Clock clock;

    private LocalDateTime applicationStartTime;
    private LocalDateTime cutOff;

    public void execute(int version) {
        logParameters();
        applicationStartTime = LocalDateTime.now(clock);
        final Stopwatch timer = Stopwatch.createStarted();
        log.info("Case-Disposer started...");
        Set<CaseData> allDeletableCases;
        Set<CaseData> simulatedCases;
        if (version == 1) {
            log.info("Running version 1...");
            List<CaseFamily> caseFamiliesDueDeletion = caseFindingService.findCasesDueDeletion();
            List<CaseFamily> deletableCasesOnly = caseFamiliesFilter.getDeletableCasesOnly(caseFamiliesDueDeletion);
            List<CaseFamily> deletableLinkedFamiliesSimulation = caseFamiliesFilter.geSimulationCasesOnly(
                caseFamiliesDueDeletion);
            allDeletableCases = getCaseData(deletableCasesOnly);
            simulatedCases = getCaseData(deletableLinkedFamiliesSimulation);
        } else {
            log.info("Running version 2...");
            allDeletableCases = caseCollectorService.getDeletableCases(parameterResolver.getDeletableCaseTypes());
            simulatedCases = caseCollectorService.getDeletableCases(
                parameterResolver.getDeletableCaseTypesSimulation());
        }

        Integer requestLimit = parameterResolver.getRequestLimit();
        processedCasesRecordHolder.setSimulatedCases(simulatedCases);

        log.info("Found deletable cases {}...", allDeletableCases.size());
        processCases(allDeletableCases, requestLimit);

        caseDeletionLoggingService.logCases();

        log.info("Case-Disposer finished.");

        log.debug(
            "Performance: Case disposer took {} to process {} cases",
            timer.stop(),
            Math.min(allDeletableCases.size(), requestLimit)
        );
    }

    private void logParameters() {
        log.info("Request limit: {}", parameterResolver.getRequestLimit());
        log.info("Deletable case types: {}", parameterResolver.getDeletableCaseTypes());
        log.info("Deletion simulated case types: {}", parameterResolver.getDeletableCaseTypesSimulation());
        parameterResolver.getElasticsearchHosts().forEach(host -> log.info("Elasticsearch host: {}", host));
        log.info("Hearing Case Type: {}", parameterResolver.getHearingCaseType());
    }

    private void processCases(final Set<CaseData> cases, int requestLimit) {
        LocalTime cutOffTime = parameterResolver.getCutOffTime();
        // check if we need to add one day to the cut off time
        int dayOffset = applicationStartTime.toLocalTime().isAfter(cutOffTime) ? 1 : 0;
        cutOff = LocalDateTime.of(applicationStartTime.plusDays(dayOffset).toLocalDate(), cutOffTime);

        final Stopwatch timer = Stopwatch.createUnstarted();
        for (CaseData caseData : cases) {
            if (requestLimit == 0 || isCutOffTimeReached()) {
                break;
            }
            try {
                timer.start();
                caseDeletionService.deleteCaseData(caseData);
            } catch (LogAndAuditException logAndAuditException) {
                log.error("Error deleting case: {} due to log and audit exception", caseData.getReference());
            }
            requestLimit--;
            processedCasesRecordHolder.addProcessedCase(caseData);
            log.debug("Performance: case {} took {} to delete", caseData.getReference(), timer.stop());
            timer.reset();
        }
    }

    private boolean isCutOffTimeReached() {
        LocalDateTime now = LocalDateTime.now(clock);
        boolean afterCutOff = now.isAfter(cutOff);

        if (afterCutOff) {
            log.info("Current time ({}) is after cut off time {}, stopping ...", now, cutOff);
        }
        return afterCutOff;
    }
}
