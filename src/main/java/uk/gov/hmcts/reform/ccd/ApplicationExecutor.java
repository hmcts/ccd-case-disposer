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
import java.util.function.Function;
import java.util.stream.Collectors;

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

        final List<CaseData> flattenedCaseFamiliesView = FLATTEN_CASE_FAMILIES_FUNCTION.apply(deletableCasesOnly);

        final List<CaseData> potentialMultiFamilyCases =
            POTENTIAL_MULTI_FAMILY_CASE_AGGREGATOR_FUNCTION.apply(deletableCasesOnly);

        Integer requestLimit = parameterResolver.getRequestLimit();

        for (CaseData subjectCaseData : potentialMultiFamilyCases) {
            final List<CaseFamily> linkedFamilies = findLinkedCaseFamilies(
                flattenedCaseFamiliesView,
                caseFamiliesDueDeletion,
                subjectCaseData
            );
            final int linkedFamilySize = FLATTEN_CASE_FAMILIES_FUNCTION.apply(linkedFamilies).size();

            // The RequestLimit specifies the total number of cases that can be deleted.
            // In some scenarios, the linkedFamilySize may exceed the requestLimit, causing the deletion to be skipped.
            // However, in other scenarios, if the linkedFamilySize is less than or equal to the requestLimit,
            // the deletion will proceed.
            if (requestLimit >= linkedFamilySize) {
                caseDeletionService.deleteLinkedCaseFamilies(linkedFamilies);
                actuallyDeletableCases.addAll(linkedFamilies);
                requestLimit -= linkedFamilySize;
            }
        }

        caseDeletionResolver.logCaseDeletion(actuallyDeletableCases, deletableLinkedFamiliesSimulation);
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
