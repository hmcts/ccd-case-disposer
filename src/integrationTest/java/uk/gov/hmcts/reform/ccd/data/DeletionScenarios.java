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
        return Stream.of(
                Arguments.of(
                        null,
                        null,
                        "scenarios/S-001-no-case-types-specified-in-delete-request.sql",
                        List.of(1L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L)),
                        List.of(1L),
                        emptyList(),
                        emptyList(),
                        Map.of("FT_MasterCaseType", emptyList()),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L))
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-002-no-cases-exist-for-the-specified-case-type-in-delete-request.sql",
                        List.of(1L),
                        Map.of("FT_MultiplePages", List.of(1504259907353529L)),
                        List.of(1L),
                        emptyList(),
                        emptyList(),
                        Map.of("FT_MultiplePages", emptyList()),
                        Map.of("FT_MultiplePages", List.of(1504259907353529L))
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-003-resolved-ttl-is-in-the-future.sql",
                        List.of(1L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L)),
                        List.of(1L),
                        emptyList(),
                        emptyList(),
                        Map.of("FT_MasterCaseType", emptyList()),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L))
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-004-no-cases-due-deletion-present.sql",
                        List.of(1L, 2L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353528L)
                        ),
                        List.of(1L, 2L),
                        emptyList(),
                        emptyList(),
                        Map.of("FT_MasterCaseType", emptyList(),
                                "FT_MultiplePages", emptyList()
                        ),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353528L)
                        )
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-005-unexpired-cases-and-nondeletable-case-types-present.sql",
                        List.of(1L, 2L, 3L, 4L, 5L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353527L, 1504259907353528L,
                                        1504259907353529L, 1504259907353526L),
                                "FT_MultiplePages", List.of(1504259907353525L)
                        ),
                        List.of(3L, 4L, 5L),
                        emptyList(),
                        List.of(1504259907353528L, 1504259907353529L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353528L, 1504259907353529L),
                                "FT_MultiplePages", emptyList()
                        ),
                        Map.of("FT_MasterCaseType", List.of(1504259907353527L, 1504259907353526L),
                                "FT_MultiplePages", List.of(1504259907353525L)
                        )
                ),
                // Scenario 6
                Arguments.of(
                        "FT_MasterCaseType, FT_MultiplePages",
                        null,
                        "scenarios/S-005-unexpired-cases-and-nondeletable-case-types-present.sql",
                        List.of(1L, 2L, 3L, 4L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353527L, 1504259907353528L,
                                        1504259907353529L, 1504259907353526L),
                                "FT_MultiplePages", List.of(1504259907353525L)
                        ),
                        List.of(3L, 4L),
                        emptyList(),
                        List.of(1504259907353528L, 1504259907353529L, 1504259907353525L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353528L, 1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353525L)
                        ),
                        Map.of("FT_MasterCaseType", List.of(1504259907353527L, 1504259907353526L),
                                "FT_MultiplePages", emptyList()
                        )
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-007-deletable-case-linked-to-nondeletable-ttl-case.sql",
                        List.of(1L, 2L, 3L, 4L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353526L, 1504259907353528L,
                                        1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353527L)
                        ),
                        List.of(1L, 3L, 4L),
                        emptyList(),
                        List.of(1504259907353528L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353528L),
                                "FT_MultiplePages", emptyList()
                        ),
                        Map.of("FT_MasterCaseType", List.of(1504259907353526L, 1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353527L)
                        )
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-008-case-due-deletion-linked-to-nondeletable-case-type-case.sql",
                        List.of(1L, 2L, 3L, 4L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353528L, 1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353527L),
                                "FT_Conditionals", List.of(1504259907353526L)
                        ),
                        List.of(1L, 3L, 4L),
                        emptyList(),
                        List.of(1504259907353528L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353528L),
                                "FT_MultiplePages", emptyList(),
                                "FT_Conditionals", emptyList()
                        ),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353527L),
                                "FT_Conditionals", List.of(1504259907353526L)
                        )
                ),
                Arguments.of(
                        "FT_MasterCaseType, FT_MultiplePages",
                        null,
                        "scenarios/S-009-nondeletable-ttl-case-linked-to-deletable-case.sql",
                        List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353523L, 1504259907353524L,
                                        1504259907353525L, 1504259907353527L,
                                        1504259907353528L, 1504259907353529L
                                ),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        ),
                        List.of(3L, 7L),
                        emptyList(),
                        List.of(1504259907353524L, 1504259907353525L,
                                1504259907353528L, 1504259907353529L, 1504259907353526L
                        ),
                        Map.of("FT_MasterCaseType", List.of(1504259907353524L, 1504259907353525L,
                                        1504259907353528L, 1504259907353529L
                                ),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        ),
                        Map.of("FT_MasterCaseType", List.of(1504259907353523L, 1504259907353527L),
                                "FT_MultiplePages", emptyList()
                        )
                ),
                Arguments.of(
                        "FT_MasterCaseType, FT_MultiplePages",
                        null,
                        "scenarios/S-010-deletable-cases-linked-to-multiple-nondeletable-cases.sql",
                        List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353523L, 1504259907353524L,
                                        1504259907353525L, 1504259907353527L,
                                        1504259907353528L, 1504259907353529L
                                ),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        ),
                        List.of(2L, 3L, 6L, 7L),
                        emptyList(),
                        List.of(1504259907353525L, 1504259907353529L, 1504259907353526L),

                        Map.of("FT_MasterCaseType", List.of(1504259907353525L, 1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        ),
                        Map.of("FT_MasterCaseType", List.of(1504259907353523L, 1504259907353524L,
                                        1504259907353527L, 1504259907353528L
                                ),
                                "FT_MultiplePages", emptyList()
                        )
                ),
                Arguments.of(
                        "FT_MasterCaseType, FT_MultiplePages",
                        null,
                        "scenarios/S-011-mix-bag-of-deletable-and-nondeletable-cases.sql",
                        List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353522L, 1504259907353523L,
                                        1504259907353524L, 1504259907353525L,
                                        1504259907353528L, 1504259907353529L
                                ),
                                "FT_MultiplePages", List.of(1504259907353518L, 1504259907353519L,
                                        1504259907353521L, 1504259907353526L,
                                        1504259907353527L
                                ),
                                "FT_Conditionals", List.of(1504259907353520L)
                        ),
                        List.of(1L, 3L, 5L, 6L, 9L, 10L),
                        emptyList(),
                        List.of(1504259907353522L, 1504259907353523L, 1504259907353528L, 1504259907353518L,
                                1504259907353519L, 1504259907353526L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353522L, 1504259907353523L,
                                        1504259907353528L),
                                "FT_MultiplePages", List.of(1504259907353518L, 1504259907353519L,
                                        1504259907353526L),
                                "FT_Conditionals", emptyList()
                        ),
                        Map.of("FT_MasterCaseType", List.of(1504259907353524L, 1504259907353525L,
                                        1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353521L, 1504259907353527L),
                                "FT_Conditionals", List.of(1504259907353520L)
                        )
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-012-multi-parent-case-when-one-parent-is-nondeletable-ttl-case.sql",
                        List.of(1L, 2L, 3L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353527L),
                                "FT_MultiplePages", List.of(1504259907353528L)
                        ),
                        List.of(1L, 2L, 3L),
                        emptyList(),
                        emptyList(),
                        Map.of("FT_MasterCaseType", emptyList(),
                                "FT_MultiplePages", emptyList()
                        ),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353527L),
                                "FT_MultiplePages", List.of(1504259907353528L)
                        )
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-013-cases-due-deletion-linked-to-third-level-deep-nondeletable-case.sql",
                        List.of(1L, 2L, 3L, 4L, 5L, 6L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L,
                                        1504259907353527L, 1504259907353525L,
                                        1504259907353524L),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        ),
                        List.of(1L, 2L, 3L, 4L, 5L, 6L),
                        emptyList(),
                        emptyList(),
                        Map.of("FT_MasterCaseType", emptyList(),
                                "FT_MultiplePages", emptyList()
                        ),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L,
                                        1504259907353527L, 1504259907353525L,
                                        1504259907353524L),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        )
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-014-nondeletable-multi-parent-case.sql",
                        List.of(1L, 2L, 3L, 4L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L, 1504259907353527L),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        ),
                        List.of(1L, 2L, 3L, 4L),
                        emptyList(),
                        emptyList(),
                        Map.of("FT_MasterCaseType", emptyList(),
                                "FT_MultiplePages", emptyList()
                        ),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L,
                                        1504259907353527L),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        )
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-015-case-links-three-levels-deep.sql",
                        List.of(1L, 2L, 3L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L,
                                1504259907353527L)),
                        emptyList(),
                        emptyList(),
                        List.of(1504259907353529L, 1504259907353528L, 1504259907353527L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L,
                                1504259907353527L)),
                        Map.of("FT_MasterCaseType", emptyList())
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-016-deletable-multi-parent-case.sql",
                        List.of(1L, 2L, 3L, 4L, 5L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L,
                                1504259907353527L, 1504259907353526L,
                                1504259907353525L)),
                        emptyList(),
                        emptyList(),
                        List.of(1504259907353529L, 1504259907353528L, 1504259907353527L,
                                1504259907353526L, 1504259907353525L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L,
                                1504259907353527L,
                                1504259907353526L, 1504259907353525L)),
                        Map.of("FT_MasterCaseType", emptyList())
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-017-cyclically-linked-cases.sql",
                        List.of(1L, 2L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L)),
                        emptyList(),
                        emptyList(),
                        List.of(1504259907353529L, 1504259907353528L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L)),
                        Map.of("FT_MasterCaseType", emptyList())
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-018-case-linked-to-itself.sql",
                        List.of(1L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L)),
                        emptyList(),
                        emptyList(),
                        List.of(1504259907353529L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L)),
                        Map.of("FT_MasterCaseType", emptyList())
                ),
                Arguments.of(
                        null,
                        "FT_MultiplePages",
                        "scenarios/S-019-simulated-case-type.sql",
                        List.of(1L, 2L),
                        Map.of("FT_MultiplePages", List.of(1504259907353529L, 1504259907353528L)),
                        List.of(1L, 2L),
                        List.of(1504259907353529L, 1504259907353528L),
                        emptyList(),
                        Map.of("FT_MultiplePages", emptyList()),
                        Map.of("FT_MultiplePages", List.of(1504259907353529L, 1504259907353528L))
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        "FT_MultiplePages",
                        "scenarios/S-020-simulated-and-deletable-case-types.sql",
                        List.of(1L, 2L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L), "FT_MultiplePages",
                                List.of(1504259907353528L)),
                        List.of(2L),
                        List.of(1504259907353528L),
                        List.of(1504259907353529L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L)),
                        Map.of("FT_MultiplePages", List.of(1504259907353528L))
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        "FT_MultiplePages",
                        "scenarios/S-021-mix-bag-of-deletable-and-simulated-cases.sql",
                        List.of(1L, 2L, 3L),
                        Map.of("FT_MultiplePages", List.of(1504259907353527L), "FT_MasterCaseType",
                                List.of(1504259907353528L, 1504259907353529L)),
                        List.of(2L, 3L),
                        emptyList(),
                        List.of(1504259907353529L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L)),
                        Map.of("FT_MultiplePages", List.of(1504259907353527L), "FT_MasterCaseType",
                                List.of(1504259907353528L))
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-022-global-search.sql",
                        List.of(1L),
                        Map.of("global_search", List.of(1504259907351111L)),
                        emptyList(),
                        emptyList(),
                        List.of(1504259907351111L),
                        Map.of("global_search", List.of(1504259907351111L)),
                        Map.of("global_search", emptyList())
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        "FT_MultiplePages",
                        "scenarios/S-023-deletable-simulated-failed-cases.sql",
                        List.of(1L, 2L, 3L),
                        Map.of("FT_MultiplePages", List.of(1504259907353527L), "FT_MasterCaseType",
                                List.of(1504259907350000L, 1504259907353529L)),
                        List.of(2L, 3L),
                        List.of(1504259907353527L),
                        List.of(1504259907353529L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L)),
                        Map.of("FT_MultiplePages", List.of(1504259907353527L), "FT_MasterCaseType",
                                List.of(1504259907350000L))
                ),
                Arguments.of(
                    "FT_MultiplePages,HearingRecordings",
                    null,
                    "scenarios/S-024-deletable-hearings-and-non-hearing-cases.sql",
                    List.of(1L, 2L, 3L),
                    Map.of(
                        "FT_MultiplePages", List.of(6446787681450550L),
                        "HearingRecordings", List.of(4331434790352648L, 7344069216690734L)),
                    emptyList(),
                    emptyList(),
                    List.of(6446787681450550L, 4331434790352648L, 7344069216690734L),
                    Map.of("HearingRecordings", List.of(4331434790352648L, 7344069216690734L),
                           "FT_MultiplePages", List.of(6446787681450550L)),
                    Map.of("FT_MultiplePages", emptyList(),"HearingRecordings", emptyList())
                ),
                Arguments.of(
                    "FT_MasterCaseType",
                    null,
                    "scenarios/S-025-delete-cases-based-on-request-limit.sql",
                    List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L),
                    Map.of("FT_MasterCaseType", List.of(1504259907351139L, 1504259907351138L,
                                                        1504259907351137L, 1504259907351136L,
                                                        1504259907351135L, 1504259907351134L,
                                                        1504259907351133L, 1504259907351132L,
                                                        1504259907351131L, 1504259907351130L,
                                                        1504259907351129L, 1504259907351128L)),
                    List.of(11L, 12L),
                    emptyList(),
                    List.of(1504259907351139L, 1504259907351138L,
                            1504259907351137L, 1504259907351136L,
                            1504259907351135L, 1504259907351134L,
                            1504259907351133L, 1504259907351132L,
                            1504259907351131L, 1504259907351130L
                            ),
                    Map.of("FT_MasterCaseType", List.of(1504259907351139L, 1504259907351138L,
                                                        1504259907351137L, 1504259907351136L,
                                                        1504259907351135L, 1504259907351134L,
                                                        1504259907351133L, 1504259907351132L,
                                                        1504259907351131L, 1504259907351130L
                                                        )),
                    Map.of("FT_MasterCaseType", List.of(1504259907351129L,1504259907351128L))
                ),
                Arguments.of(
                    null,
                    "FT_MultiplePages",
                    "scenarios/S-027-simulated-case-more-than-request-limit.sql",
                    List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L),
                    Map.of("FT_MultiplePages", List.of(1504259907351159L, 1504259907351158L,
                                                        1504259907351157L, 1504259907351156L,
                                                        1504259907351155L, 1504259907351154L,
                                                        1504259907351153L, 1504259907351152L,
                                                        1504259907351151L, 1504259907351150L,
                                                        1504259907351149L, 1504259907351148L)),
                    List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L),
                    List.of(1504259907351157L, 1504259907351156L, 1504259907351159L,
                            1504259907351158L, 1504259907351153L, 1504259907351152L,
                            1504259907351155L, 1504259907351154L, 1504259907351149L,
                            1504259907351151L, 1504259907351150L, 1504259907351148L),
                    emptyList(),
                    Map.of("FT_MultiplePages", emptyList()),
                    Map.of("FT_MultiplePages", List.of(1504259907351159L, 1504259907351158L,
                                                       1504259907351157L, 1504259907351156L,
                                                       1504259907351155L, 1504259907351154L,
                                                       1504259907351153L, 1504259907351152L,
                                                       1504259907351151L, 1504259907351150L,
                                                       1504259907351149L, 1504259907351148L))

                ),
                Arguments.of(
                    "FT_MasterCaseType",
                    null,
                    "scenarios/S-028-cases-links-to-mutiple-childs-within-request-limit.sql",
                    List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L),
                    Map.of("FT_MasterCaseType", List.of(1504259907351238L, 1504259907351237L,
                                                        1504259907351236L, 1504259907351235L,
                                                        1504259907351234L, 1504259907351233L,
                                                        1504259907351232L, 1504259907351231L,
                                                        1504259907351230L, 1504259907351229L,
                                                        1504259907351228L)),
                    List.of(11L),
                    emptyList(),
                    List.of(1504259907351238L, 1504259907351237L, 1504259907351236L,
                            1504259907351235L, 1504259907351234L, 1504259907351233L,
                            1504259907351232L, 1504259907351231L, 1504259907351230L,
                            1504259907351229L),
                    Map.of("FT_MasterCaseType", List.of(1504259907351238L, 1504259907351237L,
                                                        1504259907351236L, 1504259907351235L,
                                                        1504259907351234L, 1504259907351233L,
                                                        1504259907351232L, 1504259907351231L,
                                                        1504259907351230L, 1504259907351229L)),
                    Map.of("FT_MasterCaseType", List.of(1504259907351228L))
                ),
                Arguments.of(
                    "FT_MasterCaseType",
                    null,
                    "scenarios/S-029-deletable-failed-cases-due-to-role-delete-exception.sql",
                    List.of(1L, 2L),
                    Map.of("FT_MasterCaseType",
                           List.of(1504259907445511L, 1504259907445512L)),
                    List.of(2L),
                    emptyList(),
                    List.of(1504259907445511L),
                    Map.of("FT_MasterCaseType", List.of(1504259907445511L,1504259907445512L)),
                    Map.of("FT_MasterCaseType", emptyList())
                ),
                Arguments.of(
                    "HearingRecordings",
                    null,
                    "scenarios/S-030-deletable-failed-cases-due-to-hearing-deletion-exception.sql",
                    List.of(1L, 2L),
                    Map.of("HearingRecordings",
                           List.of(1504259907445513L, 1504259907445514L)),
                    List.of(2L),
                    emptyList(),
                    List.of(1504259907445513L),
                    Map.of("HearingRecordings", List.of(1504259907445513L,1504259907445514L)),
                    Map.of("HearingRecordings", emptyList())
                ),
                Arguments.of(
                    "FT_ElasticCaseType",
                    null,
                    "scenarios/S-031-deletable-failed-cases-due-to-elastic-search-exception.sql",
                    List.of(1L, 2L),
                    Map.of("FT_MasterCaseType",
                           List.of(1504259907445515L, 1504259907445516L)),
                    List.of(1L, 2L),
                    emptyList(),
                    List.of(),
                    Map.of("FT_ElasticCaseType", emptyList()),
                    Map.of("FT_ElasticCaseType", emptyList())
                ),
                Arguments.of(
                    "FT_MasterCaseType",
                    null,
                    "scenarios/S-032-linked-cases-link-to-same-root-case.sql",
                    List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L),
                    Map.of("FT_MasterCaseType", List.of(
                        1504259907032001L, 1504259907032002L, 1504259907032003L, 1504259907032004L, 1504259907032005L,
                        1504259907032006L, 1504259907032007L, 1504259907032008L, 1504259907032009L, 1504259907032010L
                    )),
                    emptyList(),
                    emptyList(),
                    List.of(
                        1504259907032001L, 1504259907032002L, 1504259907032003L, 1504259907032004L, 1504259907032005L,
                        1504259907032006L, 1504259907032007L, 1504259907032008L, 1504259907032009L, 1504259907032010L
                    ),
                    Map.of("FT_MasterCaseType", List.of(
                        1504259907032001L, 1504259907032002L, 1504259907032003L, 1504259907032004L, 1504259907032005L,
                        1504259907032006L, 1504259907032007L, 1504259907032008L, 1504259907032009L, 1504259907032010L
                    )),
                    Map.of("FT_MasterCaseType", emptyList())
                ),
                Arguments.of(
                    "FT_MasterCaseType",
                    null,
                    "scenarios/S-033-linked-cases-link-to-same-root-case-with-leaf-non-deletable.sql",
                    List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L),
                    Map.of("FT_MasterCaseType", List.of(
                        1504259907033001L, 1504259907033002L, 1504259907033003L, 1504259907033004L, 1504259907033005L,
                        1504259907033006L, 1504259907033007L, 1504259907033008L, 1504259907033009L, 1504259907033010L
                    )),
                    List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L),
                    emptyList(),
                    emptyList(),
                    Map.of("FT_MasterCaseType", emptyList()),
                    Map.of("FT_MasterCaseType", List.of(
                        1504259907033001L, 1504259907033002L, 1504259907033003L, 1504259907033004L, 1504259907033005L,
                        1504259907033006L, 1504259907033007L, 1504259907033008L, 1504259907033009L, 1504259907033010L
                    ))
                ),
                Arguments.of(
                    "TaskCaseType",
                    null,
                    "scenarios/S-034-deletable-failed-cases-due-to-task-delete-exception.sql",
                    List.of(1L, 2L),
                    Map.of("TaskCaseType", List.of(1504259907351192L, 1504259907351193L)),
                    List.of(2L),
                    emptyList(),
                    List.of(1504259907351192L),
                    Map.of("TaskCaseType", List.of(1504259907351192L, 1504259907351193L)),
                    Map.of("TaskCaseType", emptyList())
                ),
                Arguments.of(
                    "FT_MasterCaseType",
                    "FT_SimulatedCaseType",
                    "scenarios/S-035-deletable-linked-to-simulated.sql",
                    List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L),
                    Map.of("FT_MasterCaseType", List.of(
                        1504259907035001L, 1504259907035002L, 1504259907035003L, 1504259907035005L,
                        1504259907035006L, 1504259907035007L, 1504259907035008L, 1504259907035009L, 1504259907035010L),
                           "FT_SimulatedCaseType", List.of(1504259907035004L)
                    ),
                    List.of(2L),
                    emptyList(),
                    List.of(1504259907351192L),
                    Map.of("TaskCaseType", List.of(1504259907351192L, 1504259907351193L)),
                    Map.of("TaskCaseType", emptyList())
                )

        );
    }
}
