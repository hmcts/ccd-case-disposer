package uk.gov.hmcts.reform.ccd.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.util.concurrent.CompletableFuture;

@Service
public class CaseDeletionAsyncService {

    private final CaseDeletionService caseDeletionService;

    public CaseDeletionAsyncService(CaseDeletionService caseDeletionService) {
        this.caseDeletionService = caseDeletionService;
    }

    @Async
    public CompletableFuture<Void> deleteCaseAsync(CaseData caseData) {
        try {
            caseDeletionService.deleteCaseData(caseData);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
