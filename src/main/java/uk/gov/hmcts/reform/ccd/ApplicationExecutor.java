package uk.gov.hmcts.reform.ccd;

import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionResolver;
import uk.gov.hmcts.reform.ccd.service.CaseDeletionService;
import uk.gov.hmcts.reform.ccd.service.CaseFinderService;
import uk.gov.hmcts.reform.ccd.util.log.CaseFamiliesFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.ccd.util.CaseFamilyUtil.FLATTEN_CASE_FAMILIES_AND_REMOVE_DUPLICATE_FUNCTION;
import static uk.gov.hmcts.reform.ccd.util.CaseFamilyUtil.FLATTEN_CASE_FAMILIES_FUNCTION;
import static uk.gov.hmcts.reform.ccd.util.CaseFamilyUtil.POTENTIAL_MULTI_FAMILY_CASE_AGGREGATOR_FUNCTION;

@Slf4j
@Named
@RequiredArgsConstructor
public class ApplicationExecutor {
    private final CaseFinderService caseFindingService;
    private final CaseDeletionResolver caseDeletionResolver;
    private final CaseDeletionService caseDeletionService;
    private final CaseFamiliesFilter caseFamiliesFilter;
    private final ParameterResolver parameterResolver;

    public void execute() {
        log.info("Case-Disposer started...");
        final List<CaseFamily> caseFamiliesDueDeletion = caseFindingService.findCasesDueDeletion();
        final List<CaseFamily> deletableCasesOnly = caseFamiliesFilter.getDeletableCasesOnly(caseFamiliesDueDeletion);
        final List<CaseFamily> deletableLinkedFamiliesSimulation = caseFamiliesFilter
            .geSimulationCasesOnly(caseFamiliesDueDeletion);

        final List<CaseFamily> actuallyDeletableCases = new ArrayList<>();


        final List<CaseData> flattenedAllDeletableCases = FLATTEN_CASE_FAMILIES_FUNCTION.apply(deletableCasesOnly);

        Integer requestLimit = parameterResolver.getRequestLimit();

        final List<List<CaseData>> potentialMultiFamilyCases = POTENTIAL_MULTI_FAMILY_CASE_AGGREGATOR_FUNCTION.apply(
            deletableCasesOnly);

        for (List<CaseData> caseDataList : potentialMultiFamilyCases) {
            final List<CaseFamily> deletableCaseFamilies = getCaseFamilies(
                caseDataList,
                flattenedAllDeletableCases,
                deletableCasesOnly
            );

            // If a root case has more than 1 child case, the linked family may be processed more than once.
            // To avoid this, we check if the family id of the root case has already been processed.
            final boolean hasNotBeenProcessed = isHasNotBeenProcessed(deletableCaseFamilies, actuallyDeletableCases);

            if (hasNotBeenProcessed) {
                // Remove potential linked cases duplicates to make sure request limit is calculated correctly
                final int linkedFamilySize = FLATTEN_CASE_FAMILIES_AND_REMOVE_DUPLICATE_FUNCTION
                    .apply(deletableCaseFamilies).size();

                // The RequestLimit defines the maximum number of cases that can be deleted.
                // If the linkedFamilySize exceeds this limit, the deletion will be skipped.
                // However, if the linkedFamilySize is within the RequestLimit, the deletion will proceed.
                if (requestLimit >= linkedFamilySize) {
                    caseDeletionService.deleteLinkedCaseFamilies(deletableCaseFamilies);
                    actuallyDeletableCases.addAll(deletableCaseFamilies);
                    requestLimit -= linkedFamilySize;
                }
            }
        }
        caseDeletionResolver.logCaseDeletion(actuallyDeletableCases, deletableLinkedFamiliesSimulation);
        log.info("Case-Disposer finished.");
    }

    private boolean isHasNotBeenProcessed(final List<CaseFamily> deletableCaseFamilies,
                                          final List<CaseFamily> actuallyDeletableCases) {
        final Set<Long> processedFamilyIds = actuallyDeletableCases.stream()
            .map(caseFamily -> caseFamily.getRootCase().getFamilyId())
            .collect(Collectors.toSet());

        return deletableCaseFamilies.stream()
            .map(caseFamily -> caseFamily.getRootCase().getFamilyId())
            .noneMatch(processedFamilyIds::contains);
    }

    private List<CaseFamily> getCaseFamilies(final List<CaseData> caseDataList,
                                             final List<CaseData> flattenedAllDeletableCases,
                                             final List<CaseFamily> allDeletableCases) {
        // Collect the family IDs of matching case data
        final Set<Long> matchingFamilyIds = getMatchingFamilyIds(caseDataList, flattenedAllDeletableCases);

        // Filter case families due for deletion that have matching family IDs
        return getMatchingCaseFamilies(allDeletableCases, matchingFamilyIds);
    }

    private Set<Long> getMatchingFamilyIds(List<CaseData> caseDataList, List<CaseData> flattenedAllDeletableCases) {
        return caseDataList.stream()
            .flatMap(caseData -> flattenedAllDeletableCases.stream()
                .filter(flattenedCase -> flattenedCase.getId().equals(caseData.getId()))
                .map(CaseData::getFamilyId))
            .collect(Collectors.toSet());
    }

    private List<CaseFamily> getMatchingCaseFamilies(final List<CaseFamily> caseFamiliesDueDeletion,
                                                     final Set<Long> matchingFamilyIds) {
        return caseFamiliesDueDeletion.stream()
            .filter(caseFamily -> matchingFamilyIds.contains(caseFamily.getRootCase().getFamilyId()))
            .toList();
    }
}
