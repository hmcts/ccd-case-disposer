package uk.gov.hmcts.reform.ccd.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.ccd.config.es.CaseDataElasticsearchOperations;
import uk.gov.hmcts.reform.ccd.data.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkPrimaryKey;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.remote.DisposeDocumentsRemoteOperation;
import uk.gov.hmcts.reform.ccd.service.remote.DisposeRoleAssignmentsRemoteOperation;
import uk.gov.hmcts.reform.ccd.service.remote.LogAndAuditRemoteOperation;
import uk.gov.hmcts.reform.ccd.util.FailedToDeleteCaseFamilyHolder;
import uk.gov.hmcts.reform.ccd.util.Snooper;

import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@Slf4j
public class CaseDeletionService {

    private final CaseDataRepository caseDataRepository;
    private final CaseEventRepository caseEventRepository;
    private final CaseLinkRepository caseLinkRepository;
    private final DisposeDocumentsRemoteOperation disposeDocumentsRemoteOperation;
    private final DisposeRoleAssignmentsRemoteOperation disposeRoleAssignmentsRemoteOperation;
    private final LogAndAuditRemoteOperation logAndAuditRemoteOperation;
    private final CaseDataElasticsearchOperations caseDataElasticsearchOperations;
    private final ParameterResolver parameterResolver;
    private final Snooper snooper;
    private final FailedToDeleteCaseFamilyHolder failedToDeleteCaseFamilyHolder;

    @Inject
    public CaseDeletionService(final CaseDataRepository caseDataRepository,
                               final CaseEventRepository caseEventRepository,
                               final CaseLinkRepository caseLinkRepository,
                               final DisposeDocumentsRemoteOperation disposeDocumentsRemoteOperation,
                               final DisposeRoleAssignmentsRemoteOperation disposeRoleAssignmentsRemoteOperation,
                               final LogAndAuditRemoteOperation logAndAuditRemoteOperation,
                               final CaseDataElasticsearchOperations caseDataElasticsearchOperations,
                               final ParameterResolver parameterResolver,
                               FailedToDeleteCaseFamilyHolder failedToDeleteCaseFamilyHolder,
                               final Snooper snooper) {
        this.caseDataRepository = caseDataRepository;
        this.caseEventRepository = caseEventRepository;
        this.caseLinkRepository = caseLinkRepository;
        this.disposeDocumentsRemoteOperation = disposeDocumentsRemoteOperation;
        this.disposeRoleAssignmentsRemoteOperation = disposeRoleAssignmentsRemoteOperation;
        this.logAndAuditRemoteOperation = logAndAuditRemoteOperation;
        this.caseDataElasticsearchOperations = caseDataElasticsearchOperations;
        this.failedToDeleteCaseFamilyHolder = failedToDeleteCaseFamilyHolder;
        this.parameterResolver = parameterResolver;
        this.snooper = snooper;
    }

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
            snooper.snoop(errorMessage, exception);
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
                snooper.snoop(errorMessage, exception);
                failedToDeleteCaseFamilyHolder.addCaseFamily(caseFamily);
            }
        });
    }

    private void deleteCaseData(final CaseData caseData) {
        final Optional<CaseDataEntity> caseDataEntity = caseDataRepository.findById(caseData.getId());
        if (caseDataEntity.isPresent()) {
            logAndAuditRemoteOperation.postCaseDeletionToLogAndAudit(caseData);
            disposeDocumentsRemoteOperation.postDocumentsDelete(caseData.getReference().toString());
            disposeRoleAssignmentsRemoteOperation.postRoleAssignmentsDelete(caseData.getReference().toString());
            caseDataElasticsearchOperations.deleteByReference(getIndex(caseData.getCaseType()),
                    caseData.getReference());
            caseEventRepository.deleteByCaseDataId(caseData.getId());
            caseDataRepository.delete(caseDataEntity.get());
        }
    }

    private String getIndex(final String caseType) {
        return String.format(parameterResolver.getCasesIndexNamePattern(), caseType).toLowerCase();
    }
}
