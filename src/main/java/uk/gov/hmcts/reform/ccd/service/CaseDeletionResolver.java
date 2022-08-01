package uk.gov.hmcts.reform.ccd.service;


import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.util.log.CaseFamiliesFilter;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CaseDeletionResolver {

    private final CaseDeletionLoggingService caseDeletionLoggingService;
    private final CaseFamiliesFilter caseFamiliesFilter;

    @Inject
    public CaseDeletionResolver(final CaseDeletionLoggingService caseDeletionLoggingService,
                                final CaseFamiliesFilter caseFamiliesFilter) {
        this.caseDeletionLoggingService = caseDeletionLoggingService;
        this.caseFamiliesFilter = caseFamiliesFilter;
    }

    public void logCaseDeletion(final List<CaseFamily> linkedFamilies) {
        final List<CaseFamily> deletableLinkedFamilies = caseFamiliesFilter.getDeletableCasesOnly(linkedFamilies);

        final List<CaseFamily> deletableLinkedFamiliesSimulation =
                caseFamiliesFilter.geSimulationCasesOnly(linkedFamilies);

        caseDeletionLoggingService.logCaseFamilies(deletableLinkedFamilies, deletableLinkedFamiliesSimulation);
    }
}
