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
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkPrimaryKey;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.service.remote.RemoteDisposeService;
import uk.gov.hmcts.reform.ccd.util.FailedToDeleteCaseFamilyHolder;

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
    private final FailedToDeleteCaseFamilyHolder failedToDeleteCaseFamilyHolder;


    @Transactional
    public void deleteLinkedCaseFamilies(@NonNull final List<CaseFamily> linkedCaseFamilies) {
        linkedCaseFamilies.forEach(this::deleteLinkedCases);
        linkedCaseFamilies.forEach(this::deleteCase);
    }

    void deleteCase(final CaseFamily caseFamily) {
        final CaseData rootCaseData = caseFamily.getRootCase();
        try {
            final List<CaseData> linkedCases = caseFamily.getLinkedCases();
            log.info("About to delete case.reference:: {}", rootCaseData.getReference());
            linkedCases.forEach(this::deleteCaseData);
            deleteCaseData(rootCaseData);
            log.info("Deleted case.reference:: {}", rootCaseData.getReference());
        } catch (final Exception exception) { // Catch all exceptions
            final String errorMessage = String.format("Could not delete case.reference:: %s",
                    rootCaseData.getReference());
            log.error(errorMessage, exception);
            failedToDeleteCaseFamilyHolder.addCaseFamily(caseFamily);
        }
    }

    void deleteLinkedCases(final CaseFamily caseFamily) {
        caseFamily.getLinkedCases().forEach(caseData -> {
            final Long parentCaseId = caseData.getParentCase().getId();
            try {
                log.info("About to delete linked case.reference:: {}", caseData.getReference());
                final CaseLinkPrimaryKey caseLinkPrimaryKey = new CaseLinkPrimaryKey(parentCaseId, caseData.getId());
                caseLinkRepository.findById(caseLinkPrimaryKey)
                        .ifPresent(caseLinkRepository::delete);
                log.info("Deleted linked case.reference:: {}", caseData.getReference());
            } catch (final Exception exception) { // Catch all exceptions
                final String errorMessage = String.format("Could not delete linked case.reference:: %s",
                        caseData.getReference());
                log.error(errorMessage, exception);
                failedToDeleteCaseFamilyHolder.addCaseFamily(caseFamily);
            }
        });
    }

    private void deleteCaseData(final CaseData caseData) {
        final Optional<CaseDataEntity> caseDataEntity = caseDataRepository.findById(caseData.getId());
        if (caseDataEntity.isPresent()) {
            remoteDisposeService.remoteDeleteAll(caseData);
            caseEventRepository.deleteByCaseDataId(caseData.getId());
            caseDataRepository.delete(caseDataEntity.get());
        }
    }
}
