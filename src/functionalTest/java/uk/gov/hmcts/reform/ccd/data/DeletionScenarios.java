package uk.gov.hmcts.reform.ccd.data;

import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

public class DeletionScenarios {
    private DeletionScenarios() {
    }

    protected static Stream<Arguments> provideCaseDeletionScenarios() {
        return Stream.of(Arguments.of(
                        "FT_MasterCaseType",
                        "FT_MultiplePages",
                        "scenarios/S-001-simulated-and-deletable-case-types.sql",
                        List.of(1L, 2L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353528L)),
                        List.of(2L),
                        List.of(2L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L)),
                        Map.of("FT_MultiplePages", List.of(1504259907353528L))
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-002-global-search.sql",
                        List.of(1L),
                        Map.of("global_search", List.of(1504259907351111L),
                                "FT_MasterCaseType", List.of(1504259907351111L)),
                        emptyList(),
                        emptyList(),
                        Map.of("global_search", List.of(1504259907351111L)),
                        Map.of("global_search", emptyList())
                )
        );
    }
}
