package uk.gov.hmcts.reform.ccd.data;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public final class ShellCaseScenarios {

    private ShellCaseScenarios() {
    }

    static Stream<Arguments> provideShellCaseScenarios() {
        return Stream.of(
            scenario("shell case is created before the original case is deleted", ShellCaseResult.SUCCESS, true),
            scenario("definition store failure prevents deletion", ShellCaseResult.MAPPING_FAILURE, false),
            scenario("shell case search failure prevents deletion", ShellCaseResult.SEARCH_FAILURE, false),
            scenario("existing shell case prevents deletion", ShellCaseResult.SHELL_ALREADY_EXISTS, false),
            scenario("original case load failure prevents deletion", ShellCaseResult.ORIGINAL_CASE_FAILURE, false),
            scenario("document hash failure prevents deletion", ShellCaseResult.DOCUMENT_HASH_FAILURE, false),
            scenario("invalid create event token prevents deletion", ShellCaseResult.EVENT_TOKEN_FAILURE, false),
            scenario("shell case creation failure prevents deletion", ShellCaseResult.CREATE_FAILURE, false)
        ).map(scenario -> Arguments.of(Named.of(scenario.name(), scenario)));
    }

    private static ShellCaseScenario scenario(String name, ShellCaseResult result, boolean deletionExpected) {
        return new ShellCaseScenario(name, result, deletionExpected);
    }

    public record ShellCaseScenario(
        String name,
        ShellCaseResult result,
        boolean deletionExpected
    ) {
    }

    public enum ShellCaseResult {
        SUCCESS,
        MAPPING_FAILURE,
        SEARCH_FAILURE,
        SHELL_ALREADY_EXISTS,
        ORIGINAL_CASE_FAILURE,
        DOCUMENT_HASH_FAILURE,
        EVENT_TOKEN_FAILURE,
        CREATE_FAILURE
    }
}
