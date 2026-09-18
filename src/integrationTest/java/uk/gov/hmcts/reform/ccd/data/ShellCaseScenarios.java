package uk.gov.hmcts.reform.ccd.data;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public final class ShellCaseScenarios {

    private ShellCaseScenarios() {
    }

    static Stream<Arguments> provideShellCaseScenarios() {
        return Stream.of(
            scenario("shell case is created before the original case is deleted", ShellCaseResult.SUCCESS, true, true),
            scenario("case in an excluded state is deleted without creating a shell case",
                ShellCaseResult.CASE_STATE_EXCLUDED, true, false),
            scenario("definition store failure prevents deletion", ShellCaseResult.MAPPING_FAILURE, false, false),
            scenario("shell case search failure prevents deletion", ShellCaseResult.SEARCH_FAILURE, false, false),
            scenario("existing shell case prevents deletion", ShellCaseResult.SHELL_ALREADY_EXISTS, false, false),
            scenario("original case load failure prevents deletion", ShellCaseResult.ORIGINAL_CASE_FAILURE, false, false),
            scenario("document hash failure prevents deletion", ShellCaseResult.DOCUMENT_HASH_FAILURE, false, false),
            scenario("invalid create event token prevents deletion", ShellCaseResult.EVENT_TOKEN_FAILURE, false, false),
            scenario("shell case creation failure prevents deletion", ShellCaseResult.CREATE_FAILURE, false, false)
        ).map(scenario -> Arguments.of(Named.of(scenario.name(), scenario)));
    }

    private static ShellCaseScenario scenario(
        String name,
        ShellCaseResult result,
        boolean deletionExpected,
        boolean shellCreationExpected
    ) {
        return new ShellCaseScenario(name, result, deletionExpected, shellCreationExpected);
    }

    public record ShellCaseScenario(
        String name,
        ShellCaseResult result,
        boolean deletionExpected,
        boolean shellCreationExpected
    ) {
    }

    public enum ShellCaseResult {
        SUCCESS,
        CASE_STATE_EXCLUDED,
        MAPPING_FAILURE,
        SEARCH_FAILURE,
        SHELL_ALREADY_EXISTS,
        ORIGINAL_CASE_FAILURE,
        DOCUMENT_HASH_FAILURE,
        EVENT_TOKEN_FAILURE,
        CREATE_FAILURE
    }
}
