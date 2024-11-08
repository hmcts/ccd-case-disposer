package uk.gov.hmcts.reform.ccd;

import jakarta.inject.Inject;
import jakarta.inject.Named;
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

import static uk.gov.hmcts.reform.ccd.util.CaseFamilyUtil.FLATTEN_CASE_FAMILY_FUNCTION;
import static uk.gov.hmcts.reform.ccd.util.CaseFamilyUtil.POTENTIAL_MULTI_FAMILY_CASE_AGGREGATOR_FUNCTION;

@Slf4j
@Named
public class ApplicationExecutor {
    private final CaseFinderService caseFindingService;
    private final CaseDeletionResolver caseDeletionResolver;
    private final CaseDeletionService caseDeletionService;
    private final CaseFamiliesFilter caseFamiliesFilter;
    private final ParameterResolver parameterResolver;

    @Inject
    public ApplicationExecutor(final CaseFinderService caseFindingService,
                               final CaseDeletionResolver caseDeletionResolver,
                               final CaseDeletionService caseDeletionService,
                               final CaseFamiliesFilter caseFamiliesFilter,
                               final ParameterResolver parameterResolver) {
        this.caseFindingService = caseFindingService;
        this.caseDeletionResolver = caseDeletionResolver;
        this.caseDeletionService = caseDeletionService;
        this.caseFamiliesFilter = caseFamiliesFilter;
        this.parameterResolver = parameterResolver;
    }

    public void execute() {
        log.info("Case-Disposer started...");
        final List<CaseFamily> caseFamiliesDueDeletion = caseFindingService.findCasesDueDeletion();
        final List<CaseFamily> deletableCasesOnly = caseFamiliesFilter.getDeletableCasesOnly(caseFamiliesDueDeletion);
        final List<CaseFamily> deletableLinkedFamiliesSimulation =
            caseFamiliesFilter.geSimulationCasesOnly(caseFamiliesDueDeletion);
        final List<CaseFamily> actuallyDeletableCases = new ArrayList<>();

        int index = 0;
        while (index < deletableCasesOnly.size() && index < parameterResolver.getRequestLimit()) {

            CaseFamily deletableCase = deletableCasesOnly.get(index);

            final List<CaseData> flattenedCaseFamiliesView = FLATTEN_CASE_FAMILY_FUNCTION.apply(deletableCase);

            final List<CaseData> potentialMultiFamilyCases =
                POTENTIAL_MULTI_FAMILY_CASE_AGGREGATOR_FUNCTION.apply(deletableCase);

            potentialMultiFamilyCases.forEach(subjectCaseData -> {
                final List<CaseFamily> linkedFamilies = findLinkedCaseFamilies(
                    flattenedCaseFamiliesView,
                    caseFamiliesDueDeletion,
                    subjectCaseData
                );
                caseDeletionService.deleteLinkedCaseFamilies(linkedFamilies);
            });
            actuallyDeletableCases.add(deletableCase);
            index++;
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
