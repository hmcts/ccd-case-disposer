package uk.gov.hmcts.reform.ccd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.shell.config.ShellCaseProperties;
import uk.gov.hmcts.reform.ccd.shell.exception.ShellAlreadyExistsException;
import uk.gov.hmcts.reform.ccd.shell.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.model.CcdCaseResponse;
import uk.gov.hmcts.reform.ccd.shell.model.ShellDecision;
import uk.gov.hmcts.reform.ccd.shell.model.ShellMappingResponse;
import uk.gov.hmcts.reform.ccd.shell.model.ShellPlan;
import uk.gov.hmcts.reform.ccd.shell.service.CcdCaseService;
import uk.gov.hmcts.reform.ccd.shell.service.ShellCaseDataMapper;
import uk.gov.hmcts.reform.ccd.shell.service.ShellDocumentService;
import uk.gov.hmcts.reform.ccd.shell.service.ShellMappingService;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class CaseDisposalWorkflow {

    private final ShellMappingService shellMappingService;
    private final CaseDeletionService caseDeletionService;
    private final CcdCaseService ccdCaseService;
    private final ShellCaseProperties shellCaseProperties;
    private final ShellCaseDataMapper shellCaseDataMapper;
    private final ShellDocumentService shellDocumentService;

    public ShellPlan preview(CaseData caseData) {
        return prepareShell(caseData);
    }

    public DisposalOutcome disposeV2(CaseData caseData) {
        ShellPlan plan = prepareShell(caseData);
        if (plan.decision() != ShellDecision.CREATE_SHELL) {
            // to be expanded
            return DisposalOutcome.SHELL_FAILED;
        }

        shellDocumentService.appendDocumentHashes(plan.shellData());
        ccdCaseService.createShellCase(caseData, plan.shellData(), plan.shellCaseType());

        caseDeletionService.deleteCaseData(caseData);
        return DisposalOutcome.DELETED;
    }

    public DisposalOutcome dispose(CaseData caseData) {
        try {
            if (shellCaseProperties.isEnabled()) {
                shellCaseFlow(caseData);
            }

            caseDeletionService.deleteCaseData(caseData);
            return DisposalOutcome.DELETED;
        } catch (ShellAlreadyExistsException exc) {
            log.error(
                "Shell case already exists. Case ref: {}, case type: {}, shell case ref: {}",
                caseData.getReference(),
                caseData.getCaseType(),
                exc.getShellCaseReference(),
                exc);
            return DisposalOutcome.SHELL_ALREADY_EXISTS;
        } catch (ShellCaseException exc) {
            log.error("Shell case creation failed for case: {}", caseData.getReference(), exc);
            return DisposalOutcome.SHELL_FAILED;
        } catch (RuntimeException exc) {
            log.error("Unexpected error occurred while disposing case: {}", caseData.getReference(), exc);
            return DisposalOutcome.SHELL_FAILED;
        }
    }

    private ShellPlan prepareShell(CaseData caseData) {
        ShellMappingResponse mapping = shellMappingService.loadMappings(caseData.getCaseType());
        if (!mappingRequireShell(mapping, caseData)) {
            return skippedShell(caseData, "NO_SHELL_MAPPING");
        }
        if (mapping.caseStates().stream().noneMatch(state -> state.name().equals(caseData.getState()))) {
            return skippedShell(caseData, "STATE_EXCLUDED");
        }

        Optional<Long> existingShellCase = ccdCaseService.findShellCase(mapping.shellCaseTypeId(), caseData.getReference());
        if (existingShellCase.isPresent()) {
            return skippedShell(caseData, "SHELL_ALREADY_EXISTS");
        }

        CcdCaseResponse originalCaseData = ccdCaseService.loadCase(caseData.getReference());

        ObjectNode mappedData = shellCaseDataMapper.map(
            originalCaseData.data(), mapping.shellCaseMappings());

        mappedData.put("original_case_reference", caseData.getReference().toString());
        mappedData.put("original_case_type", caseData.getCaseType());
        return new ShellPlan(
            caseData.getReference(),
            mapping.shellCaseTypeId(),
            ShellDecision.CREATE_SHELL,
            mappedData,
            null
        );
    }


    private void shellCaseFlow(CaseData caseData) {
        ShellMappingResponse shellMapping = shellMappingService.loadMappings(caseData.getCaseType());

        if (!mappingRequireShell(shellMapping, caseData)) {
            return;
        }

        String shellCaseType = shellMapping.shellCaseTypeId();
        Optional<Long> existingShellCase = ccdCaseService.findShellCase(shellCaseType, caseData.getReference());
        if (existingShellCase.isPresent()) {
            log.error("Found shell case for case type: {}", caseData.getCaseType());
            throw new ShellAlreadyExistsException(caseData.getReference(), existingShellCase.get());
        }

        CcdCaseResponse originalCaseData = ccdCaseService.loadCase(caseData.getReference());

        ObjectNode mappedData = shellCaseDataMapper.map(
            originalCaseData.data(), shellMapping.shellCaseMappings());

        shellDocumentService.appendDocumentHashes(mappedData);
        ccdCaseService.createShellCase(caseData, mappedData, shellCaseType);

        log.info("Created shell case for original case reference: {}", caseData.getReference());
    }

    private boolean mappingRequireShell(ShellMappingResponse shellMapping, CaseData caseData) {

        String shellCaseType = shellMapping.shellCaseTypeId();
        if (shellCaseType == null) {
            log.info("No shell case mapping found for case type: {}", caseData.getCaseType());
            return false;
        }

        boolean stateMapped = shellMapping.caseStates().stream()
            .anyMatch(caseState -> caseState.name().equals(caseData.getState()));
        if (!stateMapped) {
            log.info("Case state {} is explicitly set to be excluded from shell creation", caseData.getState());
        }
        return stateMapped;
    }

    private ShellPlan skippedShell(CaseData caseData, String reason) {
        return new ShellPlan(
            caseData.getReference(),
            null,
            ShellDecision.SKIP_SHELL,
            null,
            reason
        );
    }

    public enum DisposalOutcome {
        DELETED,
        SHELL_ALREADY_EXISTS,
        SHELL_FAILED
    }
}
