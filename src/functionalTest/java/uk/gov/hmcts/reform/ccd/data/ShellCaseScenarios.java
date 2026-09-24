package uk.gov.hmcts.reform.ccd.data;

import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;

public final class ShellCaseScenarios {

    private ShellCaseScenarios() {
    }

    static Stream<Arguments> provideShellCaseScenarios() {
        return Stream.of(Arguments.of(
            "DPR_FT_MasterCaseType",
            "scenarios/shell/S-001-case-deleted-and-shell-case-created.sql",
            List.of(1004259907353629L),
            Map.of("DPR_FT_MasterCaseType", List.of(1004259907353629L)),
            List.of(1004259907353629L),
            Map.of(1004259907353629L, List.of("jpg.jpg")),
            Map.of(1004259907353629L, List.of("S-002-role-assignment-for-shell-case.json")),
            Map.of("DPR_FT_MasterCaseType", List.of(1004259907353629L)),
            emptyMap(),
            List.of(1004259907353629L),
            "DPR_FT_Shell_MasterCaseType"
            )
        );
    }
}
