package uk.gov.hmcts.reform.ccd.config.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.indices.RefreshRequest;
import co.elastic.clients.elasticsearch.indices.RefreshResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pivovarit.function.ThrowingConsumer;
import jakarta.inject.Inject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.fixture.CaseDataEntityBuilder;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.with;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Component
public class ElasticSearchIndexCreator {

    @Inject
    private ElasticsearchClient elasticsearchClient;

    @Inject
    private ParameterResolver parameterResolver;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void insertDataIntoElasticsearch(final String indexName, final List<Long> caseRefs) throws IOException {
        final String caseIndex = getIndexName(indexName);
        final List<CaseDataEntity> caseDataEntities = buildCaseDataEntity(
            getIndexName(indexName),
            caseRefs);

        final BulkRequest.Builder bulkRequestBuilder = new BulkRequest.Builder();
        caseDataEntities.forEach(ThrowingConsumer.unchecked(data -> {
            bulkRequestBuilder.operations(op -> op
                .index(idx -> idx
                    .index(caseIndex)
                    .document(data) // Pass the object directly
                )
            );
        }));

        final BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequestBuilder.build());

        assertFalse(bulkResponse.errors());

        refreshIndex(caseIndex);
    }

    private List<CaseDataEntity> buildCaseDataEntity(final String caseType, final List<Long> caseRefs) {
        return caseRefs.stream()
                .map(ref -> new CaseDataEntityBuilder(ref)
                        .withReference(ref)
                        .withCaseType(caseType)
                        .build())
                .collect(Collectors.toList());
    }

    private void refreshIndex(final String caseIndex) throws IOException {
        final RefreshRequest request = RefreshRequest.of(r -> r.index(caseIndex));
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
