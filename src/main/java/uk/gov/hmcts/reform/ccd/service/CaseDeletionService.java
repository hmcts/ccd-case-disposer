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
        deleteLinkedCases(caseData);
        deleteCase(caseData);
    }

    void deleteLinkedCases(final CaseData caseData) {
        try {
            log.info("About to delete linked case.reference:: {}", caseData.getReference());

            final List<CaseLinkEntity> allLinkedCases = caseLinkRepository.findByCaseIdOrLinkedCaseId(caseData.getId());
            if (!allLinkedCases.isEmpty()) {
                caseLinkRepository.deleteAll(allLinkedCases);
            }

            log.info("Deleted linked case.reference:: {}", caseData.getReference());

        } catch (final Exception exception) { // Catch all exceptions
            final String errorMessage = String.format(
                "Could not delete linked case.reference:: %s",
                caseData.getReference()
            );
            log.error(errorMessage, exception);
            processedCasesRecordHolder.addFailedToDeleteCaseRef(caseData);
        }
    }

    void deleteCase(final CaseData caseData) {
        try {
            log.info("About to delete case.reference:: {}", caseData.getReference());

            final Optional<CaseDataEntity> caseDataEntity = caseDataRepository.findById(caseData.getId());
            if (caseDataEntity.isPresent()) {
                remoteDisposeService.remoteDeleteAll(caseData);
                caseEventRepository.deleteByCaseDataId(caseData.getId());
                caseDataRepository.delete(caseDataEntity.get());
                logAndAuditRemoteOperation.postCaseDeletionToLogAndAudit(caseData);
            }

            log.info("Deleted case.reference:: {}", caseData.getReference());

        } catch (final Exception exception) { // Catch all exceptions
            final String errorMessage = String.format(
                "Could not delete case.reference:: %s",
                caseData.getReference()
            );
            log.error(errorMessage, exception);
            processedCasesRecordHolder.addFailedToDeleteCaseRef(caseData);
        }
    }
}
