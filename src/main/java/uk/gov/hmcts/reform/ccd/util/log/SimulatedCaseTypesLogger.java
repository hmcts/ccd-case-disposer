package uk.gov.hmcts.reform.ccd.util.log;

import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Named
@Slf4j
@RequiredArgsConstructor
public class SimulatedCaseTypesLogger {

    private final ParameterResolver parameterResolver;

    public void logSimulatedCaseTypeInAppInsights(final List<CaseFamily> caseFamilies) {
        if (!parameterResolver.getDeletableCaseTypesSimulation().isEmpty()) {
            caseFamilies.forEach(caseFamily -> {
                final Set<Long> familyCaseRefs =
                    Stream.of(List.of(caseFamily.getRootCase()), caseFamily.getLinkedCases())
                        .flatMap(Collection::stream)
                        .map(CaseData::getReference)
                        .collect(toSet());

                final String logMessage = String.format("Simulated case type: %s, Case refs: %s",
                                                        caseFamily.getRootCase().getCaseType(), familyCaseRefs);

                log.info(logMessage);
            });

        }
    }
}
