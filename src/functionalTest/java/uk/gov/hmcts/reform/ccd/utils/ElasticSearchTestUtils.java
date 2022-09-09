package uk.gov.hmcts.reform.ccd.utils;

import com.pivovarit.function.ThrowingConsumer;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
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

    @Inject
    private RestHighLevelClient elasticsearchClient;

    @Inject
    private ParameterResolver parameterResolver;

    public void verifyElasticsearchRecords(final Map<String, Integer> numberOfElasticSearchRecords) {
        if (!isPreview()) {
            numberOfElasticSearchRecords.entrySet().forEach(ThrowingConsumer.unchecked(entry ->
                    with().await().untilAsserted(() -> {
                        Thread.sleep(10000);
                        final String indexName = getIndexName(entry.getKey());
                        refreshIndex(indexName);
                        final long totalNumberOfRecordsByIndexName = findTotalNumberOfRecordsByIndexName(indexName);
                        assertThat(totalNumberOfRecordsByIndexName).isEqualTo(Long.valueOf(entry.getValue()));
                    })));
        }
    }

    private long findTotalNumberOfRecordsByIndexName(final String caseIndex) throws IOException {
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().from(0);

        final SearchRequest searchRequest = new SearchRequest(caseIndex)
                .types(INDEX_TYPE)
                .source(searchSourceBuilder);

        final SearchResponse searchResponse = elasticsearchClient.search(searchRequest, DEFAULT);

        return Arrays.stream(searchResponse.getHits().getHits()).count();
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

    private boolean isPreview() {
        final Optional<String> env = Optional.ofNullable(System.getenv("ENV"));
        if (env.isPresent() && env.get().equals("preview")) {
            return true;
        }
        return false;
    }

}