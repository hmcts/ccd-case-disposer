package uk.gov.hmcts.reform.ccd.service;


import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.util.log.CaseFamiliesFilter;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CaseDeletionResolver {

    private final CaseDeletionSimulationService caseDeletionSimulationService;
    private final CaseFamiliesFilter caseFamiliesFilter;

    @Inject
    public CaseDeletionResolver(final CaseDeletionSimulationService caseDeletionSimulationService,
                                final CaseFamiliesFilter caseFamiliesFilter) {
        this.caseDeletionSimulationService = caseDeletionSimulationService;
        this.caseFamiliesFilter = caseFamiliesFilter;
    }

    public void simulateCaseDeletion(final List<CaseFamily> linkedFamilies) {
        final List<CaseFamily> deletableLinkedFamilies = caseFamiliesFilter.getDeletableCasesOnly(linkedFamilies);

        final List<CaseFamily> deletableLinkedFamiliesSimulation =
                caseFamiliesFilter.geSimulationCasesOnly(linkedFamilies);

        caseDeletionSimulationService.logCaseFamilies(deletableLinkedFamilies, deletableLinkedFamiliesSimulation);
    }
}
