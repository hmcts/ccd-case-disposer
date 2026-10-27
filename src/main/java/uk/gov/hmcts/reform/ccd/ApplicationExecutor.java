package uk.gov.hmcts.reform.ccd;

import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.JobInterruptedException;
import uk.gov.hmcts.reform.ccd.exception.LogAndAuditException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionLoggingService;
import uk.gov.hmcts.reform.ccd.service.CaseDisposalWorkflow;
import uk.gov.hmcts.reform.ccd.service.v2.CaseCollectorService;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;
import uk.gov.hmcts.reform.ccd.util.perf.LogExecutionTime;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Named
@RequiredArgsConstructor
public class ApplicationExecutor {
    private final CaseDisposalWorkflow caseDisposalWorkflow;
    private final ParameterResolver parameterResolver;
    private final ProcessedCasesRecordHolder processedCasesRecordHolder;
    private final CaseDeletionLoggingService caseDeletionLoggingService;
    private final CaseCollectorService caseCollectorService;
    @Qualifier("caseDeletionExecutor")
    private final ThreadPoolTaskExecutor taskExecutor;
    private final Clock clock;

    private LocalDateTime applicationStartTime;
    private LocalDateTime cutOff;

    @LogExecutionTime("Case-disposer")
    public void execute() {

        logParameters();
        applicationStartTime = LocalDateTime.now(clock);
        log.info("Case-Disposer started...");
        Set<CaseData> allDeletableCases = caseCollectorService.getDeletableCases(
            parameterResolver.getDeletableCaseTypes());
        Set<CaseData> simulatedCases = caseCollectorService.getDeletableCases(
            parameterResolver.getDeletableCaseTypesSimulation());

        Integer requestLimit = parameterResolver.getRequestLimit();
        processedCasesRecordHolder.setSimulatedCases(simulatedCases);

        log.info("Found deletable cases {}...", allDeletableCases.size());
        processCases(allDeletableCases, requestLimit);

        caseDeletionLoggingService.logCases();

        log.info("Case-Disposer finished.");

    }

    private void logParameters() {
        log.info("Request limit: {}", parameterResolver.getRequestLimit());
        log.info("Deletable case types: {}", parameterResolver.getDeletableCaseTypes());
        log.info("Deletion simulated case types: {}", parameterResolver.getDeletableCaseTypesSimulation());
        parameterResolver.getElasticsearchHosts().forEach(host -> log.info("Elasticsearch host: {}", host));
        log.info("Hearing Case Type: {}", parameterResolver.getHearingCaseType());
    }

    @SuppressWarnings({"PMD.ExceptionAsFlowControl", "PMD.CognitiveComplexity"})
    private void processCases(final Set<CaseData> cases, int requestLimit) {
        int remainingRequests = requestLimit;
        LocalTime cutOffTime = parameterResolver.getCutOffTime();
        // check if we need to add one day to the cut off time
        int dayOffset = applicationStartTime.toLocalTime().isAfter(cutOffTime) ? 1 : 0;
        cutOff = LocalDateTime.of(applicationStartTime.plusDays(dayOffset).toLocalDate(), cutOffTime);

        List<Future<?>> futures = new ArrayList<>();

        for (CaseData caseData : cases) {
            if (remainingRequests <= 0 || isCutOffTimeReached()) {
                break;
            }

            Future<?> future = taskExecutor.submit(() -> {
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Case deletion interrupted");
                    }
                    CaseDisposalWorkflow.DisposalOutcome outcome = caseDisposalWorkflow.dispose(caseData);
                    if (outcome != CaseDisposalWorkflow.DisposalOutcome.DELETED) {
                        processedCasesRecordHolder.addFailedToDeleteCaseRef(caseData);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // VERY important
                    log.warn("Case deletion interrupted for case {}", caseData.getReference());
                } catch (LogAndAuditException e) {
                    processedCasesRecordHolder.addFailedToDeleteCaseRef(caseData);
                    log.error("Error deleting case {}", caseData.getReference(), e);
                } catch (RuntimeException exc) {
                    processedCasesRecordHolder.addFailedToDeleteCaseRef(caseData);
                    log.error("Unexpected error deleting case {}", caseData.getReference(), exc);
                } finally {
                    processedCasesRecordHolder.addProcessedCase(caseData);
                }
            });

            futures.add(future);
            remainingRequests--;
        }
        waitForCompletion(futures);
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    private void waitForCompletion(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get(); // blocks
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                cancelOutstanding(futures);
                throw new JobInterruptedException();
            } catch (ExecutionException e) {
                log.error("Task failed {}", e.getCause(), e);
            }
        }
    }

    private void cancelOutstanding(List<Future<?>> futures) {
        futures.forEach(f -> f.cancel(true));
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
