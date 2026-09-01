package uk.gov.hmcts.reform.ccd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.CaseDeletionException;
import uk.gov.hmcts.reform.ccd.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.config.ShellCaseProperties;
import uk.gov.hmcts.reform.ccd.shell.data.CcdCaseResponse;
import uk.gov.hmcts.reform.ccd.shell.model.ShellMappingResponse;
import uk.gov.hmcts.reform.ccd.shell.service.OriginalCaseDataLoader;
import uk.gov.hmcts.reform.ccd.shell.service.ShellMappingService;

@RequiredArgsConstructor
@Service
@Slf4j
public class CaseDisposalWorkflow {
    private final ShellMappingService shellMappingService;
    private final CaseDeletionService caseDeletionService;
    private final OriginalCaseDataLoader originalCaseDataLoader;
    private final ShellCaseProperties shellCaseProperties;

    public DisposalOutcome dispose(CaseData caseData) {
        try {
            if (shellCaseProperties.isEnabled()) {
                shellCaseFlow(caseData);
            }

            caseDeletionService.deleteCaseData(caseData);
            return DisposalOutcome.DELETED;
        } catch (ShellCaseException exc) {
            log.error("Shell case creation failed for case: {}", caseData.getReference(), exc);
            return DisposalOutcome.SHELL_FAILED;
        } catch (CaseDeletionException exc) {
            log.error("Case deletion failed for case: {}", caseData.getReference(), exc);
            return DisposalOutcome.DELETION_FAILED;
        }
    }

    private void shellCaseFlow(CaseData caseData) {
        ShellMappingResponse definition = shellMappingService.loadMappings(caseData.getCaseType());
        if (definition.getShellCaseTypeID() != null) {
            CcdCaseResponse originalCaseData = originalCaseDataLoader.load(caseData.getReference());
            log.info("ORIGINAL CASE - {}", originalCaseData.toString());
        }
    }

    public enum DisposalOutcome {
        DELETED,
        SHELL_FAILED,
        DELETION_FAILED
    }

}
