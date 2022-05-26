package uk.gov.hmcts.reform.ccd.utils;

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
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.with;
import static org.elasticsearch.client.RequestOptions.DEFAULT;

@Component
public class ElasticSearchTestUtils {

    private static final String INDEX_TYPE = "_doc";
    private static final String CASE_REFERENCE_FIELD = "reference";

    @Inject
    private RestHighLevelClient elasticsearchClient;

    @Inject
    private ParameterResolver parameterResolver;

    public void verifyElasticsearchDeletion(final Map<String, List<Long>> deletedFromIndexed,
                                            final Map<String, List<Long>> notDeletedFromIndexed) {
        verifyCaseDataAreDeletedInElasticsearch(deletedFromIndexed);
        verifyCaseDataAreInElasticsearch(notDeletedFromIndexed);
    }

    private void verifyCaseDataAreDeletedInElasticsearch(final Map<String, List<Long>> deletedFromIndexed) {
        deletedFromIndexed.forEach((key, value) -> {
            final String indexName = getIndexName(key);

            value.forEach(ThrowingConsumer.unchecked(caseReference -> {
                refreshIndex(indexName);
                final Optional<Long> actualCaseReference = findCaseByReference(indexName, caseReference);

                with()
                        .await()
                        .untilAsserted(() -> assertThat(actualCaseReference).isNotPresent());
            }));
        });
    }

    public List<String> getAllDocuments(final String indexName) throws IOException {
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery());
        final SearchRequest searchRequest = new SearchRequest(indexName)
                .source(searchSourceBuilder);

        final SearchResponse searchResponse = elasticsearchClient.search(searchRequest, DEFAULT);

        return Arrays.stream(searchResponse.getHits().getHits())
                .filter(hit -> indexName.startsWith(hit.getIndex()))
                .map(SearchHit::getId)
                .collect(Collectors.toUnmodifiableList());
    }

    public void resetIndices(final Set<String> caseTypes) throws Exception {
        final BulkRequest bulkRequest = new BulkRequest()
                .setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);

        caseTypes.forEach(ThrowingConsumer.unchecked(caseType -> {

            final String indexName = getIndexName(caseType);
            if (elasticsearchClient.indices().exists(new GetIndexRequest(indexName), DEFAULT)) {
                final List<String> documents = getAllDocuments(indexName);
                documents.forEach(documentId -> {
                    final DeleteRequest deleteRequest = new DeleteRequest(indexName)
                            .id(documentId)
                            .type(parameterResolver.getCasesIndexType());
                    bulkRequest.add(deleteRequest);
                });
            }
        }));

        if (bulkRequest.numberOfActions() > 0) {
            final BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest, DEFAULT);

            if (bulkResponse.hasFailures()) {
                throw new Exception("Errors resetting indices::: " + bulkResponse.buildFailureMessage());
            }
        }
    }

    public void verifyCaseDataAreInElasticsearch(final Map<String, List<Long>> indexedData) {
        indexedData.forEach((key, value) -> {
            final String indexName = getIndexName(key);

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

        final SearchResponse searchResponse = elasticsearchClient.search(searchRequest, DEFAULT);
        final Optional<String> first = Arrays.stream(searchResponse.getHits().getHits())
                .map(SearchHit::getId)
                .findFirst();

        return first.map(ThrowingFunction.unchecked(id -> {
            final GetRequest getRequest = new GetRequest(caseIndex, INDEX_TYPE, id);
            final GetResponse getResponse = elasticsearchClient.get(getRequest, DEFAULT);

            if (!getResponse.isExists()) {
                return null;
            }
            final Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
            return (Long) sourceAsMap.get(CASE_REFERENCE_FIELD);
        }));
    }

    private void refreshIndex(final String index) throws IOException {
        final RefreshRequest request = new RefreshRequest(index);
        elasticsearchClient.indices().refresh(request, DEFAULT);
    }


    public String getIndexName(String caseType) {
        if (!parameterResolver.getGlobalSearchIndexName().equals(caseType)) {
            return String.format("%s_cases", caseType.toLowerCase());
        }
        return caseType;
    }

}