package uk.gov.hmcts.reform.ccd.service;


import jakarta.inject.Inject;
import jakarta.inject.Named;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.service.remote.LogAndAuditRemoteOperation;
import uk.gov.hmcts.reform.ccd.util.FailedToDeleteCaseFamilyHolder;
import uk.gov.hmcts.reform.ccd.util.LogAndAuditCaseFilter;
import uk.gov.hmcts.reform.ccd.util.log.CaseFamiliesFilter;

import java.util.List;

@Named
public class CaseDeletionResolver {

    private final CaseDeletionLoggingService caseDeletionLoggingService;
    private final CaseFamiliesFilter caseFamiliesFilter;
    private final FailedToDeleteCaseFamilyHolder failedToDeleteCaseFamilyHolder;
    private final LogAndAuditRemoteOperation logAndAuditRemoteOperation;
    private final LogAndAuditCaseFilter logAndAuditCaseFilter;

    @Inject
    public CaseDeletionResolver(final CaseDeletionLoggingService caseDeletionLoggingService,
                                final CaseFamiliesFilter caseFamiliesFilter,
                                final FailedToDeleteCaseFamilyHolder failedToDeleteCaseFamilyHolder,
                                final LogAndAuditRemoteOperation logAndAuditRemoteOperation,
                                final LogAndAuditCaseFilter logAndAuditCaseFilter) {
        this.caseDeletionLoggingService = caseDeletionLoggingService;
        this.caseFamiliesFilter = caseFamiliesFilter;
        this.failedToDeleteCaseFamilyHolder = failedToDeleteCaseFamilyHolder;
        this.logAndAuditRemoteOperation = logAndAuditRemoteOperation;
        this.logAndAuditCaseFilter = logAndAuditCaseFilter;
    }

    public void logCaseDeletion(final List<CaseFamily> deletableCases,
                                final List<CaseFamily> deletableLinkedFamiliesSimulation) {

        final List<CaseFamily> deletedLinkedFamilies = caseFamiliesFilter
                .filterSuccessfulCaseFamiliesByCaseRef(deletableCases,
                        failedToDeleteCaseFamilyHolder.getCaseRefs());

        sendSuccessfullyDeletedCasesToLogAndAudit(deletedLinkedFamilies);

        caseDeletionLoggingService.logCaseFamilies(deletedLinkedFamilies,
                                                   deletableLinkedFamiliesSimulation,
                failedToDeleteCaseFamilyHolder.getFailedToDeleteCaseFamilies());
    }

    private void sendSuccessfullyDeletedCasesToLogAndAudit(final List<CaseFamily> deletedLinkedFamilies) {
        final List<CaseData> distinctCaseDataFromCaseFamilyList =
                logAndAuditCaseFilter.getDistinctCaseDataFromCaseFamilyList(deletedLinkedFamilies);
        distinctCaseDataFromCaseFamilyList.forEach(logAndAuditRemoteOperation::postCaseDeletionToLogAndAudit);
    }
}
