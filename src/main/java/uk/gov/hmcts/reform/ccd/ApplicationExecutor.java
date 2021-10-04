package uk.gov.hmcts.reform.ccd;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionService;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
@Named
public class ApplicationExecutor {
    private final CaseDeletionService caseDeletionService;

    @Inject
    public ApplicationExecutor(CaseDeletionService caseDeletionService) {
        this.caseDeletionService = caseDeletionService;
    }

    public void execute() {
        log.info("Case-Disposer started...");
        final List<CaseDataEntity> expiredCases = caseDeletionService.getExpiredCases();
        expiredCases.forEach(caseDeletionService::deleteCase);
        log.info("Case-Disposer finished.");
    }

}
