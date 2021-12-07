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
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
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

    private static final String INDEX_TYPE = "_doc";
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
                List.of(1L),
                Map.of("FT_MasterCaseType", List.of(1504259907353529L)),
                List.of(1L),
                Map.of("FT_MasterCaseType", emptyList()),
                Map.of("FT_MasterCaseType", List.of(1504259907353529L))
            ),
            Arguments.of(
                "FT_MasterCaseType",
                "scenarios/S-002.sql",
                List.of(1L),
                Map.of("FT_MultiplePages", List.of(1504259907353529L)),
                List.of(1L),
                Map.of("FT_MultiplePages", emptyList()),
                Map.of("FT_MultiplePages", List.of(1504259907353529L))
            ),
            Arguments.of(
                "FT_MasterCaseType",
                "scenarios/S-003.sql",
                List.of(1L),
                Map.of("FT_MasterCaseType", List.of(1504259907353529L)),
                List.of(1L),
                Map.of("FT_MasterCaseType", emptyList()),
                Map.of("FT_MasterCaseType", List.of(1504259907353529L))
            ),
            Arguments.of(
                "FT_MasterCaseType",
                "scenarios/S-004.sql",
                List.of(1L, 2L),
                Map.of("FT_MasterCaseType", List.of(1504259907353529L),
                       "FT_MultiplePages", List.of(1504259907353528L)
                ),
                List.of(1L, 2L),
                Map.of("FT_MasterCaseType", emptyList(),
                       "FT_MultiplePages", emptyList()
                ),
                Map.of("FT_MasterCaseType", List.of(1504259907353529L),
                       "FT_MultiplePages", List.of(1504259907353528L)
                )
            ),
            Arguments.of(
                "FT_MasterCaseType",
                "scenarios/S-005.sql",
                List.of(1L, 2L, 3L, 4L),
                Map.of("FT_MasterCaseType", List.of(1504259907353527L, 1504259907353528L, 1504259907353529L),
                       "FT_MultiplePages", List.of(1504259907353526L)
                ),
                List.of(3L, 4L),
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
                "scenarios/S-005.sql",
                List.of(1L, 2L, 3L, 4L),
                Map.of("FT_MasterCaseType", List.of(1504259907353527L, 1504259907353528L, 1504259907353529L),
                       "FT_MultiplePages", List.of(1504259907353526L)
                ),
                List.of(3L),
                Map.of("FT_MasterCaseType", List.of(1504259907353528L, 1504259907353529L),
                       "FT_MultiplePages", List.of(1504259907353526L)
                ),
                Map.of("FT_MasterCaseType", List.of(1504259907353527L),
                       "FT_MultiplePages", emptyList()
                )
            ),
            Arguments.of(
                "FT_MasterCaseType",
                "scenarios/S-007.sql",
                List.of(1L, 2L, 3L, 4L),
                Map.of("FT_MasterCaseType", List.of(1504259907353526L, 1504259907353528L, 1504259907353529L),
                       "FT_MultiplePages", List.of(1504259907353527L)
                ),
                List.of(1L, 3L, 4L),
                Map.of("FT_MasterCaseType", List.of(1504259907353528L),
                       "FT_MultiplePages", emptyList()
                ),
                Map.of("FT_MasterCaseType", List.of(1504259907353526L, 1504259907353529L),
                       "FT_MultiplePages", List.of(1504259907353527L)
                )
            ),
            Arguments.of(
                "FT_MasterCaseType",
                "scenarios/S-008.sql",
                List.of(1L, 2L, 3L, 4L),
                Map.of("FT_MasterCaseType", List.of(1504259907353528L, 1504259907353529L),
                       "FT_MultiplePages", List.of(1504259907353527L),
                       "FT_Conditionals", List.of(1504259907353526L)
                ),
                List.of(1L, 3L, 4L),
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
                "scenarios/S-009.sql",
                List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L),
                Map.of("FT_MasterCaseType", List.of(1504259907353523L, 1504259907353524L, 1504259907353525L,
                                                    1504259907353527L, 1504259907353528L, 1504259907353529L
                       ),
                       "FT_MultiplePages", List.of(1504259907353526L)
                ),
                List.of(3L, 7L),
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
                "scenarios/S-010.sql",
                List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L),
                Map.of("FT_MasterCaseType", List.of(1504259907353523L, 1504259907353524L, 1504259907353525L,
                                                    1504259907353527L, 1504259907353528L, 1504259907353529L
                       ),
                       "FT_MultiplePages", List.of(1504259907353526L)
                ),
                List.of(2L, 3L, 6L, 7L),
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
                "scenarios/S-011.sql",
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
                "scenarios/S-012.sql",
                List.of(1L, 2L, 3L),
                Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353527L),
                       "FT_MultiplePages", List.of(1504259907353528L)
                ),
                List.of(1L, 2L, 3L),
                Map.of("FT_MasterCaseType", emptyList(),
                       "FT_MultiplePages", emptyList()
                ),
                Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353527L),
                       "FT_MultiplePages", List.of(1504259907353528L)
                )
            ),
            Arguments.of(
                "FT_MasterCaseType",
                "scenarios/S-013.sql",
                List.of(1L, 2L, 3L, 4L),
                Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L, 1504259907353527L),
                       "FT_MultiplePages", List.of(1504259907353526L)
                ),
                List.of(1L, 2L, 3L, 4L),
                Map.of("FT_MasterCaseType", emptyList(),
                       "FT_MultiplePages", emptyList()
                ),
                Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L, 1504259907353527L),
                       "FT_MultiplePages", List.of(1504259907353526L)
                )
            ),
            Arguments.of(
                "FT_MasterCaseType",
                "scenarios/S-014.sql",
                List.of(1L, 2L, 3L, 4L),
                Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L, 1504259907353527L),
                       "FT_MultiplePages", List.of(1504259907353526L)
                ),
                List.of(1L, 2L, 3L, 4L),
                Map.of("FT_MasterCaseType", emptyList(),
                       "FT_MultiplePages", emptyList()
                ),
                Map.of("FT_MasterCaseType", List.of(1504259907353529L, 1504259907353528L, 1504259907353527L),
                       "FT_MultiplePages", List.of(1504259907353526L)
                )
            )
        );
    }

    protected void setupData(final String deletableCaseTypes,
                             final String scriptPath,
                             final List<Long> rowIds,
                             final Map<String, List<Long>> indexedData) throws Exception {
        System.clearProperty(DELETABLE_CASE_TYPES_PROPERTY);
        resetIndices(indexedData.keySet());

        setDeletableCaseTypes(deletableCaseTypes);
        insertDataIntoDatabase(scriptPath);
        verifyDatabaseIsPopulated(rowIds);
        verifyCaseDataAreInElasticsearch(indexedData);
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

    private void verifyDatabaseIsPopulated(final List<Long> rowIds) {
        rowIds.forEach(item -> {
            Optional<CaseDataEntity> caseDataToDelete = caseDataRepository.findById(item);
            assertThat(caseDataToDelete).isPresent();
        });
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

    protected void verifyElasticsearchDeletion(final Map<String, List<Long>> deletedFromIndexed,
                                               final Map<String, List<Long>> notDeletedFromIndexed) {
        verifyCaseDataAreDeletedInElasticsearch(deletedFromIndexed);
        verifyCaseDataAreInElasticsearch(notDeletedFromIndexed);
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

}
