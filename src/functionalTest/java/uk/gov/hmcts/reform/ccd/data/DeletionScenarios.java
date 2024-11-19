package uk.gov.hmcts.reform.ccd.data;

import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public class DeletionScenarios {

    private DeletionScenarios() {
    }

    protected static Stream<Arguments> provideCaseDeletionScenarios() {
        return Stream.of(Arguments.of(
                        "DPR_FT_MasterCaseType",
                        "DPR_FT_MultiplePages",
                        "scenarios/S-001-simulated-and-deletable-case-types.sql",
                        List.of(1004259907353529L, 1004259907353528L),
                        Map.of("DPR_FT_MasterCaseType", List.of(1004259907353529L),
                                "DPR_FT_MultiplePages", List.of(1004259907353528L)),
                        List.of(1004259907353528L),
                        List.of(1004259907353528L),
                        Map.of(1004259907353529L, List.of("jpg.jpg")),
                        Map.of(1004259907353529L, List.of("S-001-role-assignment-for-case.json")),
                        Map.of("DPR_FT_MasterCaseType", List.of(1004259907353529L)),
                        Map.of("DPR_FT_MultiplePages", List.of(1004259907353528L)),
                        List.of(1004259907353529L)
                ),
                Arguments.of(
                        "DPR_FT_MasterCaseType",
                        null,
                        "scenarios/S-002-global-search.sql",
                        List.of(1004259907351111L),
                        Map.of("global_search", List.of(1004259907351111L)),
                        emptyList(),
                        emptyList(),
                        emptyMap(),
                        emptyMap(),
                        Map.of("global_search", List.of(1004259907351111L)),
                        Map.of("global_search", emptyList()),
                        List.of(1004259907351111L)
                )/*,
                 Arguments.of(
                     "DPR_FT_HearingCaseType",
                     null,
                     "scenarios/S-003-hearing-deletable-case-type",
                     List.of(1004259907359998L),
                     Map.of("DPR_FT_HearingCaseType", List.of(1004259907359998L)),
                     emptyList(),
                     emptyList(),
                     emptyMap(),
                     emptyMap(),
                     Map.of("DPR_FT_HearingCaseType", List.of(1004259907359998L)),
                     emptyMap(),
                     List.of(1004259907359998L)
                 )*/

        );
    }
}
