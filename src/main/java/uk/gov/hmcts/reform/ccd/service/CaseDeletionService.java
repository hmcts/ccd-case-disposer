package uk.gov.hmcts.reform.ccd.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.ccd.config.es.CaseDataElasticsearchOperations;
import uk.gov.hmcts.reform.ccd.data.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkPrimaryKey;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.exception.CaseDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.remote.DisposeDocumentsRemoteOperation;
import uk.gov.hmcts.reform.ccd.service.remote.DisposeRoleAssignmentsRemoteOperation;
import uk.gov.hmcts.reform.ccd.util.Snooper;

import java.util.List;
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
    private final CaseDataElasticsearchOperations caseDataElasticsearchOperations;
    private final ParameterResolver parameterResolver;
    private final Snooper snooper;

    @Inject
    public CaseDeletionService(final CaseDataRepository caseDataRepository,
                               final CaseEventRepository caseEventRepository,
                               final CaseLinkRepository caseLinkRepository,
                               final DisposeDocumentsRemoteOperation disposeDocumentsRemoteOperation,
                               final DisposeRoleAssignmentsRemoteOperation disposeRoleAssignmentsRemoteOperation,
                               final CaseDataElasticsearchOperations caseDataElasticsearchOperations,
                               final ParameterResolver parameterResolver,
                               final Snooper snooper) {
        this.caseDataRepository = caseDataRepository;
        this.caseEventRepository = caseEventRepository;
        this.caseLinkRepository = caseLinkRepository;
        this.disposeDocumentsRemoteOperation = disposeDocumentsRemoteOperation;
        this.disposeRoleAssignmentsRemoteOperation = disposeRoleAssignmentsRemoteOperation;
        this.caseDataElasticsearchOperations = caseDataElasticsearchOperations;
        this.parameterResolver = parameterResolver;
        this.snooper = snooper;
    }

    @Transactional
    public void deleteLinkedCaseFamilies(@NonNull final List<CaseFamily> linkedCaseFamilies) {
        linkedCaseFamilies.forEach(caseFamily -> deleteLinkedCases(caseFamily.getLinkedCases()));
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
        } catch (Exception e) { // Catch all exception
            final String errorMessage = String.format("Could not delete case.reference:: %s",
                                                      rootCaseData.getReference());
            snooper.snoop(errorMessage, e);
            throw new CaseDeletionException(errorMessage, e);
        }
    }

    void deleteLinkedCases(final List<CaseData> linkedCases) {
        linkedCases.forEach(item -> deleteLinkedCase(item.getParentCase().getId(), item));
    }

    private void deleteLinkedCase(final Long parentCaseId, final CaseData caseData) {
        try {
            log.info("About to delete linked case.reference:: {}", caseData.getReference());
            final CaseLinkPrimaryKey caseLinkPrimaryKey = new CaseLinkPrimaryKey(parentCaseId, caseData.getId());
            caseLinkRepository.findById(caseLinkPrimaryKey)
                .ifPresent(caseLinkRepository::delete);
            log.info("Deleted linked case.reference:: {}", caseData.getReference());
        } catch (Exception e) { // Catch all exception
            final String errorMessage = String.format("Could not delete linked case.reference:: %s",
                                                      caseData.getReference());
            snooper.snoop(errorMessage, e);
            throw new CaseDeletionException(errorMessage, e);
        }
    }

    private void deleteCaseData(final CaseData caseData) {
        caseEventRepository.deleteByCaseDataId(caseData.getId());
        caseDataRepository.findById(caseData.getId()).ifPresent(caseDataRepository::delete);
        disposeDocumentsRemoteOperation.postDocumentsDelete(caseData.getReference().toString());
        disposeRoleAssignmentsRemoteOperation.postRoleAssignmentsDelete(caseData.getReference().toString());
        caseDataElasticsearchOperations.deleteByReference(getIndex(caseData.getCaseType()), caseData.getReference());
    }

    private String getIndex(final String caseType) {
        return String.format(parameterResolver.getCasesIndexNamePattern(), caseType).toLowerCase();
    }
}
