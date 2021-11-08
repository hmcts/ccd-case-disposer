package uk.gov.hmcts.reform.ccd;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionService;
import uk.gov.hmcts.reform.ccd.service.CaseFindingService;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
@Named
public class ApplicationExecutor {
    private final CaseFindingService caseFindingService;
    private final CaseDeletionService caseDeletionService;

    @Inject
    public ApplicationExecutor(final CaseFindingService caseFindingService,
                               final CaseDeletionService caseDeletionService) {
        this.caseFindingService = caseFindingService;
        this.caseDeletionService = caseDeletionService;
    }

    public void execute() {
        log.info("Case-Disposer started...");
        final List<CaseDataEntity> deletableCandidates = caseFindingService.findDeletableCandidates();
        deletableCandidates.forEach(caseDeletionService::deleteCase);
        log.info("Case-Disposer finished.");
    }

}
