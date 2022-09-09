package uk.gov.hmcts.reform.ccd.data;

import org.junit.jupiter.params.provider.Arguments;

import java.util.Map;
import java.util.stream.Stream;

public class DeletionScenarios {

    private DeletionScenarios() {
    }

    protected static Stream<Arguments> provideCaseDeletionScenarios() {
        return Stream.of(Arguments.of(
                        "DPR_FT_MasterCaseType",
                        "DPR_FT_MultiplePages",
                        Map.of("DPR_FT_MasterCaseType", 2, "DPR_FT_MultiplePages", 1),
                        Map.of("DPR_FT_MasterCaseType", 0, "DPR_FT_MultiplePages", 1),
                        Map.of("DPR_FT_MasterCaseType", "jpg.jpg"),
                        Map.of("DPR_FT_MasterCaseType", "S-001-role-assignment-for-case.json")
                )
        //Arguments.of(
        //"DPR_FT_MasterCaseType",
        //null,
        //"scenarios/S-002-global-search.sql",
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
