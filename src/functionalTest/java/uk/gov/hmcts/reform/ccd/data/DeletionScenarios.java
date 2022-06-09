package uk.gov.hmcts.reform.ccd.data;

import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DeletionScenarios {

    private DeletionScenarios() {
    }

    protected static Stream<Arguments> provideCaseDeletionScenarios() {
        return Stream.of(Arguments.of(
                        "DPR_FT_MasterCaseType",
                        "DPR_FT_MultiplePages",
                        "scenarios/S-001-simulated-and-deletable-case-types.sql",
                        List.of(1004259907353529L, 1004259907353528L),
                        Map.of("DPR_FT_MasterCaseType", List.of(1004259907353529L)),
                        List.of(1004259907353528L),
                        List.of(1004259907353528L),
                        Map.of(1004259907353529L, List.of("jpg.jpg")),
                        Map.of("DPR_FT_MasterCaseType", List.of(1004259907353529L)),
                        Map.of("DPR_FT_MultiplePages", List.of(1004259907353528L))
                )
        //Arguments.of(
        //"DPR_FT_MasterCaseType",
        //null,
        //"scenarios/S-002-global-search.sql",1504259907353529
        //List.of(1004259907351111L),
        //Map.of("global_search", List.of(1004259907351111L)),
        //emptyList(),
        //emptyList(),
        //emptyMap(),
        //Map.of("global_search", List.of(1004259907351111L)),
        //Map.of("global_search", emptyList())
        //)
        );
    }
}
