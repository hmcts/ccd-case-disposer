package uk.gov.hmcts.reform.ccd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.CaseDeletionException;
import uk.gov.hmcts.reform.ccd.exception.ShellAlreadyExistsException;
import uk.gov.hmcts.reform.ccd.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.config.ShellCaseProperties;
import uk.gov.hmcts.reform.ccd.shell.data.CcdCaseResponse;
import uk.gov.hmcts.reform.ccd.shell.model.ShellCasePayload;
import uk.gov.hmcts.reform.ccd.shell.model.ShellDocument;
import uk.gov.hmcts.reform.ccd.shell.model.ShellMappingResponse;
import uk.gov.hmcts.reform.ccd.shell.service.CaseDocumentResolver;
import uk.gov.hmcts.reform.ccd.shell.service.DocumentHashService;
import uk.gov.hmcts.reform.ccd.shell.service.OriginalCaseDataLoader;
import uk.gov.hmcts.reform.ccd.shell.service.ShellCaseCreator;
import uk.gov.hmcts.reform.ccd.shell.service.ShellCaseDataMapper;
import uk.gov.hmcts.reform.ccd.shell.service.ShellCaseFinder;
import uk.gov.hmcts.reform.ccd.shell.service.ShellDocumentHashAppender;
import uk.gov.hmcts.reform.ccd.shell.service.ShellMappingService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class CaseDisposalWorkflow {

    private final ShellMappingService shellMappingService;
    private final CaseDeletionService caseDeletionService;
    private final OriginalCaseDataLoader originalCaseDataLoader;
    private final ShellCaseFinder shellCaseFinder;
    private final ShellCaseProperties shellCaseProperties;
    private final ShellCaseDataMapper shellCaseDataMapper;
    private final CaseDocumentResolver caseDocumentResolver;
    private final DocumentHashService documentHashService;
    private final ShellDocumentHashAppender shellDocumentHashAppender;
    private final ShellCaseCreator shellCaseCreator;

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
        } catch (CaseDeletionException exc) {
            log.error("Case deletion failed for case: {}", caseData.getReference(), exc);
            return DisposalOutcome.DELETION_FAILED;
        }
    }

    private void shellCaseFlow(CaseData caseData) {
        ShellMappingResponse shellMapping = shellMappingService.loadMappings(caseData.getCaseType());
        String shellCaseType = shellMapping.getShellCaseTypeID();
        if (shellCaseType == null) {
            log.info("No shell case mapping found for case type: {}", caseData.getCaseType());
            return;
        }

        CcdCaseResponse originalCaseData = originalCaseDataLoader.load(caseData.getReference());

        Optional<Long> foundShellCases = shellCaseFinder.findShellCase(shellCaseType, caseData.getReference());
        if (foundShellCases.isPresent()) {
            log.error("Found shell case for case type: {}", caseData.getCaseType());
            throw new ShellAlreadyExistsException(caseData.getReference(), foundShellCases.get());
        }

        ObjectNode mappedData = shellCaseDataMapper.map(
            originalCaseData.data(), shellMapping.getShellCaseMappings());
        List<ShellDocument> documents = caseDocumentResolver.resolveDocuments(mappedData);
        Map<UUID, String> hashes = documentHashService.fetchHashes(documents);
        shellDocumentHashAppender.appendHash(documents, hashes);

        ShellCasePayload shellCase = shellCaseCreator.build(caseData, mappedData, shellCaseType);
        shellCaseCreator.create(shellCase, shellCaseType);

        log.info("Created shell case for original case reference: {}", caseData.getReference());

    }

    public enum DisposalOutcome {
        DELETED,
        SHELL_ALREADY_EXISTS,
        SHELL_FAILED,
        DELETION_FAILED
    }

}
