package uk.gov.hmcts.reform.ccd.utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.indices.RefreshRequest;
import co.elastic.clients.elasticsearch.indices.RefreshResponse;
import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingFunction;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.with;

@SuppressWarnings("unchecked")
@Component
@Slf4j
public class ElasticSearchTestUtils {

    private static final String INDEX_TYPE = "_doc";
    private static final String CASE_REFERENCE_FIELD = "reference";

    @Inject
    private ElasticsearchClient elasticsearchClient;

    @Inject
    private ParameterResolver parameterResolver;

    public void verifyElasticsearchDeletion(final Map<String, List<Long>> deletedFromIndexed,
                                            final Map<String, List<Long>> notDeletedFromIndexed) {
        verifyCaseDataAreDeletedInElasticsearch(deletedFromIndexed);
        verifyCaseDataAreInElasticsearch(notDeletedFromIndexed);
    }

    private void verifyCaseDataAreDeletedInElasticsearch(final Map<String, List<Long>> deletedFromIndexed) {

        refreshAllIndexes(deletedFromIndexed.keySet());

        deletedFromIndexed.forEach((key, value) -> {
            final String indexName = getIndexName(key);
            value.forEach(ThrowingConsumer.unchecked(caseReference -> {
                with()
                        .await()
                        .untilAsserted(() -> {
                            final Optional<Long> actualCaseReference = findCaseByReference(indexName,
                                    caseReference);

                            assertThat(actualCaseReference).isNotPresent();
                        });
            }));
        });
    }

    private void refreshAllIndexes(final Set<String> indexes) {
        indexes.forEach(ThrowingConsumer.unchecked(index -> {
            final String indexName = getIndexName(index);
            refreshIndex(indexName);
        }));
    }

    public List<String> getAllDocuments(final String indexName) throws IOException {
        final SearchRequest searchRequest = SearchRequest.of(s -> s
            .index(indexName)
            .query(q -> q
                .matchAll(m -> m)
            )
            .source(SourceConfig.of(sc -> sc.fetch(true)))
        );

        final SearchResponse<Map> searchResponse = elasticsearchClient.search(searchRequest, Map.class);

        return searchResponse.hits().hits().stream()
            .filter(hit -> indexName.startsWith(hit.index()))
            .map(Hit::id)
            .collect(Collectors.toUnmodifiableList());
    }

    public void resetIndices(final Set<String> caseTypes) throws Exception {
        for (String caseType : caseTypes) {
            final String indexName = getIndexName(caseType);
            if (elasticsearchClient.indices().exists(e -> e.index(indexName)).value()) {
                final List<String> documents = getAllDocuments(indexName);
                for (String documentId : documents) {
                    elasticsearchClient.delete(d -> d
                        .index(indexName)
                        .id(documentId)
                    );
                }
            }
        }
    }

    public void verifyCaseDataAreInElasticsearch(final Map<String, List<Long>> indexedData) {

        refreshAllIndexes(indexedData.keySet());

        indexedData.forEach((key, value) -> {
            final String indexName = getIndexName(key);

            value.forEach(ThrowingConsumer.unchecked(caseReference -> {
                with()
                        .await()
                        .untilAsserted(() -> {
                            final Optional<Long> actualCaseReference = findCaseByReference(indexName,
                                    caseReference);

                            assertThat(actualCaseReference)
                                    .isPresent()
                                    .hasValue(caseReference);
                        });
            }));
        });
    }

    private Optional<Long> findCaseByReference(final String caseIndex, final Long caseReference) throws IOException {
        final SearchRequest searchRequest = SearchRequest.of(s -> s
            .index(caseIndex)
            .query(q -> q
                .term(t -> t
                    .field(CASE_REFERENCE_FIELD)
                    .value(caseReference)
                )
            )
            .from(0)
        );

        final SearchResponse<Map> searchResponse = elasticsearchClient.search(searchRequest, Map.class);
        final Optional<String> first = searchResponse.hits().hits().stream()
            .map(Hit::id)
            .findFirst();

        return first.map(ThrowingFunction.unchecked(id -> {
            final GetRequest getRequest = GetRequest.of(g -> g
                .index(caseIndex)
                .id(id)
            );
            final GetResponse<Map> getResponse = elasticsearchClient.get(getRequest, Map.class);

            if (!getResponse.found()) {
                return null;
            }
            final Map<String, Object> sourceAsMap = getResponse.source();
            return (Long) sourceAsMap.get(CASE_REFERENCE_FIELD);
        }));
    }

    private void refreshIndex(final String index) throws IOException {
        final RefreshRequest request = RefreshRequest.of(r -> r.index(index));
        final RefreshResponse refreshResponse = elasticsearchClient.indices().refresh(request);

        with()
            .await()
            .untilAsserted(() -> assertThat(refreshResponse.shards().failures().size())
                .isEqualTo(0));
    }


    public String getIndexName(String caseType) {
        if (!parameterResolver.getGlobalSearchIndexName().equals(caseType)) {
            return String.format("%s_cases", caseType.toLowerCase());
        }
        return caseType;
    }
}
