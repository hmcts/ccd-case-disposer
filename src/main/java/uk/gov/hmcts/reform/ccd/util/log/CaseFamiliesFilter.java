package uk.gov.hmcts.reform.ccd.util.log;

import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Named
public class CaseFamiliesFilter {

    private ParameterResolver parameterResolver;

    @Inject
    public CaseFamiliesFilter(final ParameterResolver parameterResolver) {
        this.parameterResolver = parameterResolver;
    }

    public List<CaseFamily> getDeletableCasesOnly(final List<CaseFamily> linkedFamilies) {
        return filterCaseFamiliesByCaseTypes(
                linkedFamilies,
                parameterResolver.getDeletableCaseTypes()
        );
    }

    public List<CaseFamily> geSimulationCasesOnly(final List<CaseFamily> linkedFamilies) {
        return filterCaseFamiliesByCaseTypes(
                linkedFamilies,
                parameterResolver.getDeletableCaseTypesSimulation()
        );
    }

    private List<CaseFamily> filterCaseFamiliesByCaseTypes(final List<CaseFamily> caseFamilies,
                                                           final List<String> caseTypes) {
        return caseFamilies.stream()
                .filter(caseFamily ->
                        caseTypes.containsAll(Stream.of(List.of(caseFamily.getRootCase()), caseFamily.getLinkedCases())
                                .flatMap(Collection::stream)
                                .map(value -> value.getCaseType())
                                .collect(toSet())))
                .collect(toList());
    }
}
