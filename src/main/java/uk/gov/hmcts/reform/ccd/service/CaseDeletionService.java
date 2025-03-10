package uk.gov.hmcts.reform.ccd.service;


import jakarta.inject.Named;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.ccd.data.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.LogAndAuditException;
import uk.gov.hmcts.reform.ccd.service.remote.LogAndAuditRemoteOperation;
import uk.gov.hmcts.reform.ccd.service.remote.RemoteDisposeService;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;

import java.util.List;
import java.util.Optional;

@Named
@Slf4j
@RequiredArgsConstructor
public class CaseDeletionService {

    private final CaseDataRepository caseDataRepository;
    private final CaseEventRepository caseEventRepository;
    private final CaseLinkRepository caseLinkRepository;
    private final RemoteDisposeService remoteDisposeService;
    private final ProcessedCasesRecordHolder processedCasesRecordHolder;
    private final LogAndAuditRemoteOperation logAndAuditRemoteOperation;

    @Transactional
    public void deleteCaseData(@NonNull final CaseData caseData) {
        if (deleteCaseLinks(caseData)) {
            deleteCase(caseData);
        }
    }

    /**
     * Break links between cases.
     * Find all the links that involve the case and delete those links
     * so we don't have any linked cases associated with the case.
     *
     * @param caseData - case to delete links for
     */
    boolean deleteCaseLinks(final CaseData caseData) {
        try {
            final List<CaseLinkEntity> allLinkedCases = caseLinkRepository.findByCaseIdOrLinkedCaseId(caseData.getId());
            if (allLinkedCases.isEmpty()) {
                log.info("No linked cases found for case reference:: {}", caseData.getReference());
            } else {
                log.info("About to delete linked case reference:: {}", caseData.getReference());
                caseLinkRepository.deleteAll(allLinkedCases);
                log.info("Deleted linked case reference:: {}", caseData.getReference());
            }
            return true;
        } catch (final Exception exception) { // Catch all exceptions
            log.error("Could not delete linked case reference:: {}", caseData.getReference(), exception);
            processedCasesRecordHolder.addFailedToDeleteCaseRef(caseData);
            return false;
        }
    }

    void deleteCase(final CaseData caseData) {
        try {
            log.info("About to delete case reference:: {}", caseData.getReference());

            final Optional<CaseDataEntity> caseDataEntity = caseDataRepository.findById(caseData.getId());
            if (caseDataEntity.isPresent()) {
                remoteDisposeService.remoteDeleteAll(caseData);
                caseEventRepository.deleteByCaseDataId(caseData.getId());
                caseDataRepository.delete(caseDataEntity.get());
                logAndAuditRemoteOperation.postCaseDeletionToLogAndAudit(caseData);
            }

            log.info("Deleted case reference:: {}", caseData.getReference());
        } catch (LogAndAuditException logAndAuditException) {
            processedCasesRecordHolder.addFailedToDeleteCaseRef(caseData);
            log.error(
                "Log and Audit exception while deleting case reference:: {}",
                caseData.getReference(),
                logAndAuditException
            );
            throw logAndAuditException;
        } catch (final Exception exception) { // Catch all exceptions
            log.error("Could not delete case reference:: {}", caseData.getReference(), exception);
            processedCasesRecordHolder.addFailedToDeleteCaseRef(caseData);
        }
    }
}
