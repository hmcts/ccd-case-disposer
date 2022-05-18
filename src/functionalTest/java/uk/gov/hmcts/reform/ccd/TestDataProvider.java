package uk.gov.hmcts.reform.ccd;

import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingFunction;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.helper.EvidenceManagementDbHelper;
import uk.gov.hmcts.reform.ccd.helper.GlobalSearchIndexCreator;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.CaseDataViewHolder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.sql.DataSource;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.with;
import static uk.gov.hmcts.reform.ccd.parameter.TestParameterResolver.DELETABLE_CASE_TYPES_PROPERTY;
import static uk.gov.hmcts.reform.ccd.parameter.TestParameterResolver.DELETABLE_CASE_TYPES_PROPERTY_SIMULATION;

public class TestDataProvider {

    private static final String INDEX_TYPE = "_doc";
    private static final String CASE_REFERENCE_FIELD = "reference";

    private static final int EM_DB_INITIAL_DOCUMENTS = 6;
    private static final String EM_DATABASE_SCRIPT = "scenarios/S-000-evidence-management-database-setup.sql";

    @Inject
    private RestHighLevelClient elasticsearchClient;

    @Inject
    private DataSource dataSource;

    @Inject
    @Qualifier("ccd")
    private DataSource ccdDataSource;

    @Inject
    @Qualifier("evidence")
    private DataSource evidenceDataSource;

    @Inject
    private CaseDataRepository caseDataRepository;

    @Inject
    private EvidenceManagementDbHelper evidenceManagementDbHelper;

    @Inject
    private ParameterResolver parameterResolver;

    @Inject
    private CaseDataViewHolder caseDataViewHolder;

    @Inject
    private GlobalSearchIndexCreator globalSearchIndexCreator;

    protected static Stream<Arguments> provideCaseDeletionScenarios() {
        return Stream.of(
                Arguments.of(
                        null,
                        null,
                        "scenarios/S-001-no-case-types-specified-in-delete-request.sql",
                        EM_DATABASE_SCRIPT,
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
                        EM_DATABASE_SCRIPT,
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
                        EM_DATABASE_SCRIPT,
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
                        EM_DATABASE_SCRIPT,
                        List.of(1L, 2L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353528L)
                        ),
                        List.of(1L, 2L), //endStateRowIds
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
                        EM_DATABASE_SCRIPT,
                        List.of(1L, 2L, 3L, 4L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353527L, 1504259907353528L, 1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        ),
                        List.of(3L, 4L),
                        emptyList(),
                        List.of("40e6215d-b5c6-4896-987c-f30f3678f618", "40e6215d-b5c6-4896-987c-f30f3678f628",
                                "40e6215d-b5c6-4896-987c-f30f3678f638", "40e6215d-b5c6-4896-987c-f30f3678f648",
                                "40e6215d-b5c6-4896-987c-f30f3678f658"),
                        Map.of("FT_MasterCaseType", List.of(1504259907353528L, 1504259907353529L),
                                "FT_MultiplePages", emptyList()
                        ),
                        Map.of("FT_MasterCaseType", List.of(1504259907353527L),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        )
                ),
                // Scenario 6
                Arguments.of(
                        "FT_MasterCaseType, FT_MultiplePages",
                        null,
                        "scenarios/S-005-unexpired-cases-and-nondeletable-case-types-present.sql",
                        EM_DATABASE_SCRIPT,
                        List.of(1L, 2L, 3L, 4L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353527L, 1504259907353528L, 1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        ),
                        List.of(3L),
                        emptyList(),
                        List.of("40e6215d-b5c6-4896-987c-f30f3678f618", "40e6215d-b5c6-4896-987c-f30f3678f628",
                                "40e6215d-b5c6-4896-987c-f30f3678f638", "40e6215d-b5c6-4896-987c-f30f3678f648",
                                "40e6215d-b5c6-4896-987c-f30f3678f658"),
                        Map.of("FT_MasterCaseType", List.of(1504259907353528L, 1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        ),
                        Map.of("FT_MasterCaseType", List.of(1504259907353527L),
                                "FT_MultiplePages", emptyList()
                        )
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-007-deletable-case-linked-to-nondeletable-ttl-case.sql",
                        EM_DATABASE_SCRIPT,
                        List.of(1L, 2L, 3L, 4L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353526L, 1504259907353528L, 1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353527L)
                        ),
                        List.of(1L, 3L, 4L),
                        emptyList(),
                        List.of("40e6215d-b5c6-4896-987c-f30f3678f648",
                                "40e6215d-b5c6-4896-987c-f30f3678f658"),
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
                        EM_DATABASE_SCRIPT,
                        List.of(1L, 2L, 3L, 4L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353528L, 1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353527L),
                                "FT_Conditionals", List.of(1504259907353526L)
                        ),
                        List.of(1L, 3L, 4L),
                        emptyList(),
                        List.of("40e6215d-b5c6-4896-987c-f30f3678f648",
                                "40e6215d-b5c6-4896-987c-f30f3678f658"),
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
                        EM_DATABASE_SCRIPT,
                        List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353523L, 1504259907353524L, 1504259907353525L,
                                        1504259907353527L, 1504259907353528L, 1504259907353529L
                                ),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        ),
                        List.of(3L, 7L),
                        emptyList(),
                        List.of("40e6215d-b5c6-4896-987c-f30f3678f618", "40e6215d-b5c6-4896-987c-f30f3678f628",
                                "40e6215d-b5c6-4896-987c-f30f3678f638", "40e6215d-b5c6-4896-987c-f30f3678f648",
                                "40e6215d-b5c6-4896-987c-f30f3678f658"),
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
                        EM_DATABASE_SCRIPT,
                        List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353523L, 1504259907353524L, 1504259907353525L,
                                        1504259907353527L, 1504259907353528L, 1504259907353529L
                                ),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        ),
                        List.of(2L, 3L, 6L, 7L),
                        emptyList(),
                        List.of("40e6215d-b5c6-4896-987c-f30f3678f618", "40e6215d-b5c6-4896-987c-f30f3678f628",
                                "40e6215d-b5c6-4896-987c-f30f3678f638"),

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
                        EM_DATABASE_SCRIPT,
                        List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353522L, 1504259907353523L, 1504259907353524L,
                                        1504259907353525L, 1504259907353528L, 1504259907353529L
                                ),
                                "FT_MultiplePages", List.of(1504259907353518L, 1504259907353519L, 1504259907353521L,
                                        1504259907353526L, 1504259907353527L
                                ),
                                "FT_Conditionals", List.of(1504259907353520L)
                        ),
                        List.of(1L, 3L, 5L, 6L, 9L, 10L),
                        emptyList(),
                        List.of("40e6215d-b5c6-4896-987c-f30f3678f648",
                                "40e6215d-b5c6-4896-987c-f30f3678f658"),
                        Map.of("FT_MasterCaseType", List.of(1504259907353522L, 1504259907353523L, 1504259907353528L),
                                "FT_MultiplePages", List.of(1504259907353518L, 1504259907353519L, 1504259907353526L),
                                "FT_Conditionals", emptyList()
                        ),
                        Map.of("FT_MasterCaseType", List.of(1504259907353524L, 1504259907353525L, 1504259907353529L),
                                "FT_MultiplePages", List.of(1504259907353521L, 1504259907353527L),
                                "FT_Conditionals", List.of(1504259907353520L)
                        )
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-012-multi-parent-case-when-one-parent-is-nondeletable-ttl-case.sql",
                        EM_DATABASE_SCRIPT,
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
                        EM_DATABASE_SCRIPT,
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
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L, 1504259907353527L),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        )
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-014-nondeletable-multi-parent-case.sql",
                        EM_DATABASE_SCRIPT,
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
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L, 1504259907353527L),
                                "FT_MultiplePages", List.of(1504259907353526L)
                        )
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-015-case-links-three-levels-deep.sql",
                        EM_DATABASE_SCRIPT,
                        List.of(1L, 2L, 3L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L, 1504259907353527L)),
                        emptyList(),
                        emptyList(),
                        List.of("40e6215d-b5c6-4896-987c-f30f3678f618", "40e6215d-b5c6-4896-987c-f30f3678f628",
                                "40e6215d-b5c6-4896-987c-f30f3678f638", "40e6215d-b5c6-4896-987c-f30f3678f648",
                                "40e6215d-b5c6-4896-987c-f30f3678f658"),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L, 1504259907353527L)),
                        Map.of("FT_MasterCaseType", emptyList())
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-016-deletable-multi-parent-case.sql",
                        EM_DATABASE_SCRIPT,
                        List.of(1L, 2L, 3L, 4L, 5L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L, 1504259907353527L,
                                1504259907353526L, 1504259907353525L)),
                        emptyList(),
                        emptyList(),
                        List.of("40e6215d-b5c6-4896-987c-f30f3678f618", "40e6215d-b5c6-4896-987c-f30f3678f628",
                                "40e6215d-b5c6-4896-987c-f30f3678f638", "40e6215d-b5c6-4896-987c-f30f3678f648",
                                "40e6215d-b5c6-4896-987c-f30f3678f658"),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L, 1504259907353527L,
                                1504259907353526L, 1504259907353525L)),
                        Map.of("FT_MasterCaseType", emptyList())
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-017-cyclically-linked-cases.sql",
                        EM_DATABASE_SCRIPT,
                        List.of(1L, 2L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L)),
                        emptyList(),
                        emptyList(),
                        List.of("40e6215d-b5c6-4896-987c-f30f3678f618", "40e6215d-b5c6-4896-987c-f30f3678f628",
                                "40e6215d-b5c6-4896-987c-f30f3678f638", "40e6215d-b5c6-4896-987c-f30f3678f648",
                                "40e6215d-b5c6-4896-987c-f30f3678f658"),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L)),
                        Map.of("FT_MasterCaseType", emptyList())
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-018-case-linked-to-itself.sql",
                        EM_DATABASE_SCRIPT,
                        List.of(1L),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L)),
                        emptyList(),
                        emptyList(),
                        List.of("40e6215d-b5c6-4896-987c-f30f3678f618", "40e6215d-b5c6-4896-987c-f30f3678f628",
                                "40e6215d-b5c6-4896-987c-f30f3678f638"),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L)),
                        Map.of("FT_MasterCaseType", emptyList())
                ),
                Arguments.of(
                        null,
                        "FT_MultiplePages",
                        "scenarios/S-019-simulated-case-type.sql",
                        EM_DATABASE_SCRIPT,
                        List.of(1L, 2L),
                        Map.of("FT_MultiplePages", List.of(1504259907353529L, 1504259907353528L)),
                        List.of(1L, 2L),
                        List.of(1L, 2L),
                        emptyList(),
                        Map.of("FT_MultiplePages", emptyList()),
                        Map.of("FT_MultiplePages", List.of(1504259907353529L, 1504259907353528L))
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        "FT_MultiplePages",
                        "scenarios/S-020-simulated-and-deletable-case-types.sql",
                        EM_DATABASE_SCRIPT,
                        List.of(1L, 2L),
                        Map.of("FT_MultiplePages", List.of(1504259907353529L)),
                        List.of(2L),
                        List.of(2L),
                        List.of("40e6215d-b5c6-4896-987c-f30f3678f618", "40e6215d-b5c6-4896-987c-f30f3678f628",
                                "40e6215d-b5c6-4896-987c-f30f3678f638"),
                        Map.of("FT_MasterCaseType", List.of(1504259907353529L)),
                        Map.of("FT_MultiplePages", List.of(1504259907353528L))
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        "FT_MultiplePages",
                        "scenarios/S-021-mix-bag-of-deletable-and-simulated-cases.sql",
                        EM_DATABASE_SCRIPT,
                        List.of(1L, 2L, 3L),
                        Map.of("FT_MultiplePages", List.of(1504259907353529L)),
                        List.of(2L, 3L),
                        emptyList(),
                        List.of("40e6215d-b5c6-4896-987c-f30f3678f618", "40e6215d-b5c6-4896-987c-f30f3678f628",
                                "40e6215d-b5c6-4896-987c-f30f3678f638"),
                        Map.of("FT_MasterCaseType", emptyList()),
                        Map.of("FT_MultiplePages", List.of(1504259907353528L, 1504259907353527L))
                ),
                Arguments.of(
                        "FT_MasterCaseType",
                        null,
                        "scenarios/S-022-global-search.sql",
                        EM_DATABASE_SCRIPT,
                        List.of(1L),
                        Map.of("global_search", List.of(1504259907351111L)),
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        Map.of("global_search", List.of(1504259907351111L)),
                        Map.of("global_search", emptyList())
                )
        );
    }

    protected void setupData(final String deletableCaseTypes,
                             final String deletableCaseTypesSimulation,
                             final String ccdScriptPath,
                             final String emScriptPath,
                             final List<Long> rowIds,
                             final Map<String, List<Long>> indexedData) throws Exception {
        System.clearProperty(DELETABLE_CASE_TYPES_PROPERTY);
        System.clearProperty(DELETABLE_CASE_TYPES_PROPERTY_SIMULATION);

        createGlobalSearchIndex(indexedData);

        //resetIndices(indexedData.keySet());

        setDeletableCaseTypes(deletableCaseTypes);
        setDeletableCaseTypesSimulation(deletableCaseTypesSimulation);
        insertDataIntoDatabase(ccdDataSource, ccdScriptPath);
        insertDataIntoDatabase(evidenceDataSource, emScriptPath);
        verifyDatabaseIsPopulated(rowIds);
        verifyEvidenceDatabaseIsPopulated();
        //verifyCaseDataAreInElasticsearch(indexedData);
    }

    private void setDeletableCaseTypes(final String value) {
        if (value != null) {
            System.setProperty(DELETABLE_CASE_TYPES_PROPERTY, value);
        }
    }

    private void setDeletableCaseTypesSimulation(final String value) {
        if (value != null) {
            System.setProperty(DELETABLE_CASE_TYPES_PROPERTY_SIMULATION, value);
        }
    }

    private void insertDataIntoDatabase(final DataSource dataSource, String scriptPath) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            final ClassPathResource resource = new ClassPathResource(scriptPath);
            ScriptUtils.executeSqlScript(connection, resource);
        }
    }

    private void createGlobalSearchIndex(final Map<String, List<Long>> indexedData) {
        globalSearchIndexCreator.createGlobalSearchIndex();
    }

    private void verifyDatabaseIsPopulated(final List<Long> rowIds) {
        rowIds.forEach(item -> {
            Optional<CaseDataEntity> caseDataToDelete = caseDataRepository.findById(item);
            assertThat(caseDataToDelete).isPresent();
        });
    }

    private void verifyEvidenceDatabaseIsPopulated() throws SQLException {
        assertThat(evidenceManagementDbHelper.getDocumentsCount(false) == EM_DB_INITIAL_DOCUMENTS);
    }

    private void verifyCaseDataAreInElasticsearch(final Map<String, List<Long>> indexedData) {
        indexedData.forEach((key, value) -> {
            final String indexName = getIndex(key);

            value.forEach(ThrowingConsumer.unchecked(caseReference -> {
                with()
                        .await()
                        .untilAsserted(() -> {
                            refreshIndex(indexName);
                            final Optional<Long> actualCaseReference = findCaseByReference(indexName, caseReference);

                            assertThat(actualCaseReference)
                                    .isPresent()
                                    .hasValue(caseReference);
                        });
            }));
        });
    }

    private Optional<Long> findCaseByReference(final String caseIndex, final Long caseReference) throws IOException {
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.termQuery(CASE_REFERENCE_FIELD, caseReference))
                .from(0);

        final SearchRequest searchRequest = new SearchRequest(caseIndex)
                .types(INDEX_TYPE)
                .source(searchSourceBuilder);

        final SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
        final Optional<String> first = Arrays.stream(searchResponse.getHits().getHits())
                .map(SearchHit::getId)
                .findFirst();

        return first.map(ThrowingFunction.unchecked(id -> {
            final GetRequest getRequest = new GetRequest(caseIndex, INDEX_TYPE, id);
            final GetResponse getResponse = elasticsearchClient.get(getRequest, RequestOptions.DEFAULT);

            if (!getResponse.isExists()) {
                return null;
            }
            final Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
            return (Long) sourceAsMap.get(CASE_REFERENCE_FIELD);
        }));
    }

    private String getIndex(String caseType) {
        if (!parameterResolver.getGlobalSearchIndexName().equals(caseType)) {
            return String.format("%s_cases", caseType.toLowerCase());
        }
        return caseType;
    }

    private void refreshIndex(final String index) throws IOException {
        final RefreshRequest request = new RefreshRequest(index);
        elasticsearchClient.indices().refresh(request, RequestOptions.DEFAULT);
    }

    private void resetIndices(final Set<String> caseTypes) throws Exception {
        final BulkRequest bulkRequest = new BulkRequest()
                .setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);

        caseTypes.forEach(ThrowingConsumer.unchecked(caseType -> {
            final String indexName = getIndex(caseType);
            final List<String> documents = getAllDocuments(indexName);

            documents.forEach(documentId -> {
                final DeleteRequest deleteRequest = new DeleteRequest(indexName)
                        .id(documentId)
                        .type(parameterResolver.getCasesIndexType());
                bulkRequest.add(deleteRequest);
            });
        }));

        if (bulkRequest.numberOfActions() > 0) {
            final BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest, RequestOptions.DEFAULT);

            if (bulkResponse.hasFailures()) {
                throw new Exception("Errors resetting indices::: " + bulkResponse.buildFailureMessage());
            }
        }
    }

    private List<String> getAllDocuments(final String indexName) throws IOException {
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery());
        final SearchRequest searchRequest = new SearchRequest(indexName)
                .source(searchSourceBuilder);

        final SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

        return Arrays.stream(searchResponse.getHits().getHits())
                .filter(hit -> indexName.startsWith(hit.getIndex()))
                .map(SearchHit::getId)
                .collect(Collectors.toUnmodifiableList());
    }

    protected void verifyDatabaseDeletion(final List<Long> rowIds) {
        final List<CaseDataEntity> all = caseDataRepository.findAll();
        final List<Long> actualRowIds = all.stream()
                .map(CaseDataEntity::getId)
                .collect(Collectors.toUnmodifiableList());

        assertThat(actualRowIds)
                .isNotNull()
                .containsExactlyInAnyOrderElementsOf(rowIds);
    }

    protected void verifyEvidenceDatabaseDeletion(List<String> deletableDocumentIds) throws SQLException {
        final List<String> documentsIds =  evidenceManagementDbHelper.getDocumentsIds(true);

        assertThat(documentsIds)
            .isNotNull()
            .containsExactlyInAnyOrderElementsOf(deletableDocumentIds);
    }

    protected void verifyElasticsearchDeletion(final Map<String, List<Long>> deletedFromIndexed,
                                               final Map<String, List<Long>> notDeletedFromIndexed) {
        //verifyCaseDataAreDeletedInElasticsearch(deletedFromIndexed);
        //verifyCaseDataAreInElasticsearch(notDeletedFromIndexed);
    }

    private void verifyCaseDataAreDeletedInElasticsearch(final Map<String, List<Long>> deletedFromIndexed) {
        deletedFromIndexed.forEach((key, value) -> {
            final String indexName = getIndex(key);

            value.forEach(ThrowingConsumer.unchecked(caseReference -> {
                refreshIndex(indexName);
                final Optional<Long> actualCaseReference = findCaseByReference(indexName, caseReference);

                with()
                        .await()
                        .untilAsserted(() -> assertThat(actualCaseReference).isNotPresent());
            }));
        });
    }

    protected void verifyDatabaseDeletionSimulation(final List<Long> simulatedEndStateRowIds) {
        assertThat(simulatedEndStateRowIds)
                .isNotNull()
                .containsExactlyInAnyOrderElementsOf(caseDataViewHolder.getSimulatedCaseIds());
    }
}
