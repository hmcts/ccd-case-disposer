package uk.gov.hmcts.reform.ccd.service;


import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.service.remote.LogAndAuditRemoteOperation;
import uk.gov.hmcts.reform.ccd.util.FailedToDeleteCaseFamilyHolder;
import uk.gov.hmcts.reform.ccd.util.LogAndAuditCaseFilter;
import uk.gov.hmcts.reform.ccd.util.log.CaseFamiliesFilter;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

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

    public void logCaseDeletion(final List<CaseFamily> linkedFamilies) {

        final List<CaseFamily> successfullyDeletedOrSimulatedCases = caseFamiliesFilter
                .filterSuccessfulCaseFamiliesByCaseRef(linkedFamilies,
                        failedToDeleteCaseFamilyHolder.getCaseRefs());

        final List<CaseFamily> deletableLinkedFamilies = caseFamiliesFilter
                .getDeletableCasesOnly(successfullyDeletedOrSimulatedCases);

        final List<CaseFamily> deletableLinkedFamiliesSimulation =
                caseFamiliesFilter.geSimulationCasesOnly(successfullyDeletedOrSimulatedCases);

        sendSuccessfullyDeletedCasesToLogAndAudit(deletableLinkedFamilies);

        caseDeletionLoggingService.logCaseFamilies(deletableLinkedFamilies,
                deletableLinkedFamiliesSimulation,
                failedToDeleteCaseFamilyHolder.getFailedToDeleteCaseFamilies());
    }

    private void sendSuccessfullyDeletedCasesToLogAndAudit(final List<CaseFamily> deletableLinkedFamilies) {
        final List<CaseData> distinctCaseDataFromCaseFamilyList =
                logAndAuditCaseFilter.getDistinctCaseDataFromCaseFamilyList(deletableLinkedFamilies);
        distinctCaseDataFromCaseFamilyList.forEach(logAndAuditRemoteOperation::postCaseDeletionToLogAndAudit);
    }
}
