package uk.gov.hmcts.reform.ccd.util.log;

import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.List;

import static java.util.stream.Collectors.toSet;

@Named
@RequiredArgsConstructor
public class CaseFamiliesFilter {

    private final ParameterResolver parameterResolver;

    public List<CaseFamily> getDeletableCasesOnly(final List<CaseFamily> linkedFamilies) {
        return filterCaseFamiliesByCaseTypes(
                linkedFamilies,
                parameterResolver.getDeletableCaseTypes()
        );
    }

    public List<CaseFamily> getSimulationCasesOnly(final List<CaseFamily> linkedFamilies) {
        return filterCaseFamiliesByCaseTypes(
                linkedFamilies,
                parameterResolver.getDeletableCaseTypesSimulation()
        );
    }

    private List<CaseFamily> filterCaseFamiliesByCaseTypes(final List<CaseFamily> caseFamilies,
                                                           final List<String> caseTypes) {
        return caseFamilies.stream()
                .filter(caseFamily ->
                        caseTypes.containsAll(
                            caseFamily.linkedCases().stream()
                                .map(CaseData::caseType)
                                .collect(toSet())))
            .toList();
    }
}
