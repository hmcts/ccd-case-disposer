package uk.gov.hmcts.reform.ccd;

import com.pivovarit.function.ThrowingConsumer;
import org.awaitility.Duration;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.fixture.CaseLinkEntityBuilder;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

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

public class TestDataProvider {

    private static final String CASE_REFERENCE_FIELD = "reference";

    @Inject
    private RestHighLevelClient elasticsearchClient;

    @Inject
    private DataSource dataSource;

    @Inject
    private CaseDataRepository caseDataRepository;

    @Inject
    private CaseEventRepository caseEventRepository;

    @Inject
    private CaseLinkRepository caseLinkRepository;

    @Inject
    private ParameterResolver parameterResolver;

    protected static Stream<Arguments> provideCaseDeletionScenarios() {
        return Stream.of(
            Arguments.of(
                null,
                "scenarios/S-001.sql",
                Map.of(TestTables.CASE_DATA, List.of(1L),
                       TestTables.CASE_EVENT, emptyList(),
                       TestTables.CASE_LINK, emptyList()),
                emptyList(),
                Map.of("FT_MasterCaseType", List.of(1504259907353529L)),
                Map.of(TestTables.CASE_DATA, List.of(1L))
            ),
            Arguments.of(
                "FT_MasterCaseType",
                "scenarios/S-002.sql",
                Map.of(TestTables.CASE_DATA, List.of(1L),
                       TestTables.CASE_EVENT, emptyList(),
                       TestTables.CASE_LINK, emptyList()),
                emptyList(),
                Map.of("FT_MultiplePages", List.of(1504259907353529L)),
                Map.of(TestTables.CASE_DATA, List.of(1L))
            ),
            Arguments.of(
                "FT_MasterCaseType",
                "scenarios/S-003.sql",
                Map.of(TestTables.CASE_DATA, List.of(1L),
                       TestTables.CASE_EVENT, emptyList(),
                       TestTables.CASE_LINK, emptyList()),
                emptyList(),
                Map.of("FT_MasterCaseType", List.of(1504259907353529L)),
                Map.of(TestTables.CASE_DATA, List.of(1L))
            ),
            Arguments.of(
                "FT_MasterCaseType",
                "scenarios/S-004.sql",
                Map.of(TestTables.CASE_DATA, List.of(1L, 2L),
                       TestTables.CASE_EVENT, emptyList(),
                       TestTables.CASE_LINK, emptyList()),
                emptyList(),
                Map.of("FT_MasterCaseType", 1504259907353529L,
                       "FT_MultiplePages", 1504259907353528L
                ),
                Map.of(TestTables.CASE_DATA, List.of(1L, 2L))
            ),
            Arguments.of(
                "FT_MasterCaseType",
                "scenarios/S-005.sql",
                Map.of(TestTables.CASE_DATA, List.of(1L, 2L, 3L, 4L),
                       TestTables.CASE_EVENT, emptyList(),
                       TestTables.CASE_LINK, emptyList()),
                emptyList(),
                Map.of("FT_MasterCaseType", List.of(1504259907353527L, 1504259907353528L, 1504259907353529L),
                       "FT_MultiplePages", List.of(1504259907353526L)
                ),
                Map.of(TestTables.CASE_DATA, List.of(3L, 4L))
            ),
            // Scenario 6
            Arguments.of(
                "FT_MasterCaseType, FT_MultiplePages",
                "scenarios/S-005.sql",
                Map.of(TestTables.CASE_DATA, List.of(1L, 2L, 3L, 4L),
                       TestTables.CASE_EVENT, emptyList(),
                       TestTables.CASE_LINK, emptyList()),
                emptyList(),
                Map.of("FT_MasterCaseType", List.of(1504259907353527L, 1504259907353528L, 1504259907353529L),
                       "FT_MultiplePages", List.of(1504259907353526L)
                ),
                Map.of(TestTables.CASE_DATA, List.of(3L))
            ),
            Arguments.of(
                "FT_MasterCaseType",
                "scenarios/S-007.sql",
                Map.of(TestTables.CASE_DATA, List.of(1L, 2L, 3L, 4L),
                       TestTables.CASE_EVENT, emptyList(),
                       TestTables.CASE_LINK, emptyList()),
                List.of(new CaseLinkEntityBuilder(1L, "FT_MultiplePages", 4L)),
                Map.of("FT_MasterCaseType", List.of(1504259907353526L, 1504259907353528L, 1504259907353529L),
                       "FT_MultiplePages", 1504259907353527L
                ),
                Map.of(TestTables.CASE_DATA, List.of(1L, 3L, 4L))
            ),
            Arguments.of(
                "FT_MasterCaseType",
                "scenarios/S-008.sql",
                Map.of(TestTables.CASE_DATA, List.of(1L, 2L, 3L, 4L),
                       TestTables.CASE_EVENT, emptyList(),
                       TestTables.CASE_LINK, emptyList()),
                List.of(new CaseLinkEntityBuilder(1L, "FT_Conditionals", 4L)),
                Map.of("FT_MasterCaseType", List.of(1504259907353528L, 1504259907353529L),
                       "FT_MultiplePages", List.of(1504259907353527L),
                       "FT_Conditionals", List.of(1504259907353526L)
                ),
                Map.of(TestTables.CASE_DATA, List.of(1L, 3L, 4L))
            ),
            Arguments.of(
                "FT_MasterCaseType, FT_MultiplePages",
                "scenarios/S-009.sql",
                Map.of(TestTables.CASE_DATA, List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L),
                       TestTables.CASE_EVENT, emptyList(),
                       TestTables.CASE_LINK, emptyList()),
                List.of(
                    new CaseLinkEntityBuilder(1L, "FT_MasterCaseType", 5L),
                    new CaseLinkEntityBuilder(2L, "FT_MasterCaseType", 6L),
                    new CaseLinkEntityBuilder(3L, "FT_MasterCaseType", 7L)
                ),
                Map.of("FT_MasterCaseType", List.of(1504259907353523L, 1504259907353524L, 1504259907353525L,
                                                    1504259907353527L, 1504259907353528L, 1504259907353529L
                       ),
                       "FT_MultiplePages", List.of(1504259907353526L)
                ),
                Map.of(TestTables.CASE_DATA, List.of(3L, 7L))
            ),
            Arguments.of(
                "FT_MasterCaseType, FT_MultiplePages",
                "scenarios/S-010.sql",
                Map.of(TestTables.CASE_DATA, List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L),
                       TestTables.CASE_EVENT, emptyList(),
                       TestTables.CASE_LINK, emptyList()),
                List.of(
                    new CaseLinkEntityBuilder(1L, "FT_MasterCaseType", 5L),
                    new CaseLinkEntityBuilder(2L, "FT_MasterCaseType", 6L),
                    new CaseLinkEntityBuilder(3L, "FT_MasterCaseType", 7L)
                ),
                Map.of("FT_MasterCaseType", List.of(1504259907353523L, 1504259907353524L, 1504259907353525L,
                                                    1504259907353527L, 1504259907353528L, 1504259907353529L
                       ),
                       "FT_MultiplePages", List.of(1504259907353526L)
                ),
                Map.of(TestTables.CASE_DATA, List.of(2L, 3L, 6L, 7L))
            ),
            Arguments.of(
                "FT_MasterCaseType, FT_MultiplePages",
                "scenarios/S-011.sql",
                Map.of(TestTables.CASE_DATA, List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L),
                       TestTables.CASE_EVENT, emptyList(),
                       TestTables.CASE_LINK, emptyList()),
                List.of(
                    new CaseLinkEntityBuilder(1L, "FT_MasterCaseType", 5L),
                    new CaseLinkEntityBuilder(1L, "FT_MasterCaseType", 6L),
                    new CaseLinkEntityBuilder(2L, "FT_MasterCaseType", 7L),
                    new CaseLinkEntityBuilder(2L, "FT_MasterCaseType", 8L),
                    new CaseLinkEntityBuilder(3L, "FT_MultiplePages", 9L),
                    new CaseLinkEntityBuilder(3L, "FT_Conditionals", 10L),
                    new CaseLinkEntityBuilder(4L, "FT_MultiplePages", 11L),
                    new CaseLinkEntityBuilder(4L, "FT_MultiplePages", 12L)
                ),
                Map.of("FT_MasterCaseType", List.of(1504259907353522L, 1504259907353523L, 1504259907353524L,
                                                    1504259907353525L, 1504259907353528L, 1504259907353529L
                       ),
                       "FT_MultiplePages", List.of(1504259907353518L, 1504259907353519L, 1504259907353521L,
                                                   1504259907353526L, 1504259907353527L
                    ),
                       "FT_Conditionals", List.of(1504259907353520L)
                ),
                Map.of(TestTables.CASE_DATA, List.of(1L, 3L, 5L, 6L, 9L, 10L))
            )
        );
    }

    protected void setupData(final String deletableCaseTypes,
                             final String scriptPath,
                             final Map<Enum<TestTables>, List<Long>> rowIds,
                             final List<CaseLinkEntity> caseLinkEntities,
                             final Map<String, List<Long>> indexedData) throws Exception {
        System.clearProperty(DELETABLE_CASE_TYPES_PROPERTY);
        //resetIndices(indexedData.keySet());

        setDeletableCaseTypes(deletableCaseTypes);
        insertDataIntoDatabase(scriptPath);
        verifyDatabaseIsPopulated(rowIds, caseLinkEntities);
        //verifyElasticsearchIndicesAreCreated(indexedData);
    }

    private void setDeletableCaseTypes(final String value) {
        if (value != null) {
            System.setProperty(DELETABLE_CASE_TYPES_PROPERTY, value);
        }
    }

    private void insertDataIntoDatabase(final String scriptPath) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            final ClassPathResource resource = new ClassPathResource(scriptPath);
            ScriptUtils.executeSqlScript(connection, resource);
        }
    }

    private void verifyDatabaseIsPopulated(final Map<Enum<TestTables>, List<Long>> rowIds,
                                           final List<CaseLinkEntity> caseLinkEntities) {
        rowIds.get(TestTables.CASE_DATA).forEach(x -> {
            Optional<CaseDataEntity> caseDataToDelete = caseDataRepository.findById(x);
            assertThat(caseDataToDelete).isPresent();
        });

        rowIds.get(TestTables.CASE_EVENT).forEach(x -> {
            Optional<CaseEventEntity> caseEventToDelete = caseEventRepository.findById(x);
            assertThat(caseEventToDelete).isPresent();
        });

        rowIds.get(TestTables.CASE_LINK).forEach(x -> {
            final List<CaseLinkEntity> entities = caseLinkRepository.findByCaseId(x);
            assertThat(entities)
                .isNotEmpty()
                .hasSameElementsAs(caseLinkEntities);
        });
    }

    private void verifyElasticsearchIndicesAreCreated(final Map<String, List<Long>> indexedData) {
        indexedData.forEach((key, value) -> {
            final String indexName = getIndex(key);

            value.forEach(ThrowingConsumer.unchecked(caseReference -> {
                refreshIndex(indexName);
                final List<String> caseReferences = findCaseByReference(indexName, caseReference);

                with()
                    .pollDelay(Duration.TEN_SECONDS)
                    .pollInterval(Duration.TEN_SECONDS)
                    .await()
                    .untilAsserted(() -> assertThat(caseReferences).isNotEmpty().containsExactly(caseReference.toString()));
            }));
        });
    }

    private List<String> findCaseByReference(final String caseIndex, final Long caseReference) throws IOException {
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .query(QueryBuilders.termQuery(CASE_REFERENCE_FIELD, caseReference))
            .from(0);

        final SearchRequest searchRequest = new SearchRequest(caseIndex)
            .types("_doc")
            .source(searchSourceBuilder);

        final SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
        return Arrays.stream(searchResponse.getHits().getHits())
            .map(SearchHit::getId)
            .collect(Collectors.toUnmodifiableList());
    }

    private String getIndex(String caseType) {
        return String.format("%s_cases", caseType.toLowerCase());
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
            refreshIndex(indexName);

            final DeleteRequest deleteRequest = new DeleteRequest(indexName)
                .type(parameterResolver.getCasesIndexType());
            bulkRequest.add(deleteRequest);
        }));

        final BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest, RequestOptions.DEFAULT);

        if (bulkResponse.hasFailures()) {
            throw new Exception("Errors resetting indices::: " + bulkResponse.buildFailureMessage());
        }
    }

    protected void verifyDatabaseDeletion(final Map<Enum<TestTables>, List<Long>> rowIds) {
        final List<Long> ids = rowIds.get(TestTables.CASE_DATA);
        final List<CaseDataEntity> all = caseDataRepository.findAll();
        final List<Long> actualRowIds = all.stream()
            .map(CaseDataEntity::getId)
            .collect(Collectors.toUnmodifiableList());

        assertThat(actualRowIds)
            .isNotNull()
            .containsExactlyInAnyOrderElementsOf(ids);
    }

    /*protected void verifyElasticsearchIndexDeletion(final Map<String, List<Long>> indexedData) {
        indexedData.forEach((key, value) -> {
            final String indexName = getIndex(key);

            value.forEach(ThrowingConsumer.unchecked(caseReference -> {
                refreshIndex(indexName);
                final List<String> caseReferences = findCaseByReference(indexName, caseReference);

                with()
                    .pollDelay(Duration.TEN_SECONDS)
                    .pollInterval(Duration.TWO_SECONDS)
                    .await()
                    .untilAsserted(() -> assertThat(caseReferences).isEmpty());
            }));
        });
    }*/

}
