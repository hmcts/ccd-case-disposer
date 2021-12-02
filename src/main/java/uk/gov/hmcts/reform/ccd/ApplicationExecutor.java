package uk.gov.hmcts.reform.ccd;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionService;
import uk.gov.hmcts.reform.ccd.service.CaseFinderService;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
@Named
public class ApplicationExecutor {
    private final CaseFinderService caseFindingService;
    private final CaseDeletionService caseDeletionService;

    @Inject
    public ApplicationExecutor(final CaseFinderService caseFindingService,
                               final CaseDeletionService caseDeletionService) {
        this.caseFindingService = caseFindingService;
        this.caseDeletionService = caseDeletionService;
    }

    public void execute() {
        log.info("Case-Disposer started...");
        final List<CaseFamily> casesDueDeletion = caseFindingService.findCasesDueDeletion();
        casesDueDeletion.forEach(caseDeletionService::deleteCase);
        log.info("Case-Disposer finished.");
    }

}
