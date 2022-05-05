package uk.gov.hmcts.reform.ccd;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionResolver;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionService;
import uk.gov.hmcts.reform.ccd.service.CaseFinderService;
import uk.gov.hmcts.reform.ccd.util.log.CaseFamiliesFilter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;

import static uk.gov.hmcts.reform.ccd.util.CaseFamilyUtil.FLATTEN_CASE_FAMILIES_FUNCTION;
import static uk.gov.hmcts.reform.ccd.util.CaseFamilyUtil.POTENTIAL_MULTI_FAMILY_CASE_AGGREGATOR_FUNCTION;

@Slf4j
@Named
public class ApplicationExecutor {
    private final CaseFinderService caseFindingService;
    private final CaseDeletionResolver caseDeletionResolver;
    private final CaseDeletionService caseDeletionService;
    private final CaseFamiliesFilter caseFamiliesFilter;

    @Inject
    public ApplicationExecutor(final CaseFinderService caseFindingService,
                               final CaseDeletionResolver caseDeletionResolver,
                               final CaseDeletionService caseDeletionService,
                               final CaseFamiliesFilter caseFamiliesFilter) {
        this.caseFindingService = caseFindingService;
        this.caseDeletionResolver = caseDeletionResolver;
        this.caseDeletionService = caseDeletionService;
        this.caseFamiliesFilter = caseFamiliesFilter;
    }

    public void execute() {
        log.info("Case-Disposer started...");
        final List<CaseFamily> caseFamiliesDueDeletion = caseFindingService.findCasesDueDeletion();
        final List<CaseFamily> deletableCasesOnly = caseFamiliesFilter.getDeletableCasesOnly(caseFamiliesDueDeletion);

        final List<CaseData> flattenedCaseFamiliesView = FLATTEN_CASE_FAMILIES_FUNCTION.apply(deletableCasesOnly);

        final List<CaseData> potentialMultiFamilyCases =
                POTENTIAL_MULTI_FAMILY_CASE_AGGREGATOR_FUNCTION.apply(deletableCasesOnly);

        potentialMultiFamilyCases.forEach(subjectCaseData -> {
            final List<CaseFamily> linkedFamilies = findLinkedCaseFamilies(flattenedCaseFamiliesView,
                                                                           caseFamiliesDueDeletion,
                                                                           subjectCaseData);

            caseDeletionService.deleteLinkedCaseFamilies(linkedFamilies);
        });

        caseDeletionResolver.simulateCaseDeletion(caseFamiliesDueDeletion);
        log.info("Case-Disposer finished.");
    }

    private List<CaseFamily> findLinkedCaseFamilies(final List<CaseData> flattenedCasesView,
                                                    final List<CaseFamily> allCaseFamilies,
                                                    final CaseData subjectCaseData) {
        return buildLinkedFamilyIdsFunction(flattenedCasesView)
                .andThen(buildLinkedFamiliesFunction(allCaseFamilies))
                .apply(subjectCaseData);
    }

    private Function<CaseData, List<Long>> buildLinkedFamilyIdsFunction(final List<CaseData> flattenedCasesView) {
        return candidateCaseData -> flattenedCasesView.stream()
                .filter(caseData -> caseData.getId().equals(candidateCaseData.getId()))
                .map(CaseData::getFamilyId)
                .collect(Collectors.toUnmodifiableList());
    }

    private Function<List<Long>, List<CaseFamily>> buildLinkedFamiliesFunction(final List<CaseFamily> allCaseFamilies) {
        return linkedCaseFamilyIds -> allCaseFamilies.stream()
                .filter(caseFamily -> linkedCaseFamilyIds.contains(caseFamily.getRootCase().getFamilyId()))
                .collect(Collectors.toUnmodifiableList());
    }
}
