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

import static uk.gov.hmcts.reform.ccd.util.CaseFamilyUtil.FLATTEN_CASE_FAMILIES_AND_REMOVE_DUPLICATE_FUNCTION;
import static uk.gov.hmcts.reform.ccd.util.CaseFamilyUtil.FLATTEN_CASE_FAMILIES_FUNCTION;
import static uk.gov.hmcts.reform.ccd.util.CaseFamilyUtil.POTENTIAL_ROOT_CASE_AGGREGATOR_FUNCTION;

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

        final List<CaseData> flattenedCaseFamiliesView = FLATTEN_CASE_FAMILIES_FUNCTION.apply(deletableCasesOnly);

        final List<CaseData> potentialMultiFamilyCases =
            POTENTIAL_ROOT_CASE_AGGREGATOR_FUNCTION.apply(deletableCasesOnly);

        Integer requestLimit = parameterResolver.getRequestLimit();

        for (CaseData subjectCaseData : potentialMultiFamilyCases) {
            // If a root case has more than 1 child case, the linked family may be processed more than once.
            // To avoid this, we check if the family id of the root case has already been processed.
            final boolean hasNotBeenProcessed = actuallyDeletableCases
                .stream().noneMatch(caseFamily -> caseFamily.getRootCase().getFamilyId()
                    .equals(subjectCaseData.getFamilyId()));

            if (hasNotBeenProcessed) {
                final List<CaseFamily> linkedFamilies = findLinkedCaseFamilies(
                    flattenedCaseFamiliesView,
                    caseFamiliesDueDeletion,
                    subjectCaseData
                );
                final int linkedFamilySize = FLATTEN_CASE_FAMILIES_AND_REMOVE_DUPLICATE_FUNCTION
                    .apply(linkedFamilies).size();

                // The RequestLimit specifies the total number of cases that can be deleted.
                // In some scenarios, the linkedFamilySize may exceed the requestLimit,
                // causing the deletion to be skipped.
                // However, in other scenarios, if the linkedFamilySize is less than or equal to
                // the requestLimit the deletion will proceed.
                if ((requestLimit >= linkedFamilySize)) {
                    caseDeletionService.deleteLinkedCaseFamilies(linkedFamilies);
                    actuallyDeletableCases.addAll(linkedFamilies);
                    requestLimit -= linkedFamilySize;
                }
            }
        }

        caseDeletionResolver.logCaseDeletion(actuallyDeletableCases, deletableLinkedFamiliesSimulation);
        log.info("Case-Disposer finished.");
    }

    private List<CaseFamily> findLinkedCaseFamilies(final List<CaseData> flattenedCasesView,
                                                    final List<CaseFamily> allCaseFamilies,
                                                    final CaseData subjectCaseData) {
        List<CaseFamily> completeCaseFamilies = new ArrayList<>();
        buildCompleteCaseFamilies(flattenedCasesView, allCaseFamilies, completeCaseFamilies, subjectCaseData);
        return completeCaseFamilies;
    }

    private void buildCompleteCaseFamilies(final List<CaseData> flattenedCasesView,
                                           final List<CaseFamily> allCaseFamilies,
                                           List<CaseFamily> completeCaseFamilies,
                                           CaseData subjectCaseData) {
        final List<Long> linkedFamilyIds = buildLinkedFamilyIds(flattenedCasesView, subjectCaseData);
        List<CaseFamily> caseFamilies = buildLinkedFamilies(allCaseFamilies, linkedFamilyIds);

        for (CaseFamily caseFamily : caseFamilies) {
            if (!completeCaseFamilies.contains(caseFamily)) {
                completeCaseFamilies.add(caseFamily);
                for (CaseData childCase : caseFamily.getLinkedCases()) {
                    buildCompleteCaseFamilies(flattenedCasesView, allCaseFamilies, completeCaseFamilies, childCase);
                }
            }
        }
    }

    private List<Long> buildLinkedFamilyIds(final List<CaseData> flattenedCasesView, final CaseData candidateCaseData) {
        return flattenedCasesView.stream()
            .filter(caseData -> caseData.getId().equals(candidateCaseData.getId()))
            .map(CaseData::getFamilyId)
            .toList();
    }

    private List<CaseFamily> buildLinkedFamilies(final List<CaseFamily> allCaseFamilies,
                                                 final List<Long> linkedCaseFamilyIds) {
        return allCaseFamilies.stream()
            .filter(caseFamily -> linkedCaseFamilyIds.contains(caseFamily.getRootCase().getFamilyId()))
            .toList();
    }
}
