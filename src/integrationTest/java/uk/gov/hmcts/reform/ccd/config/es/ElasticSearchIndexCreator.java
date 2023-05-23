package uk.gov.hmcts.reform.ccd.config.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pivovarit.function.ThrowingConsumer;
import jakarta.inject.Inject;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.fixture.CaseDataEntityBuilder;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Component
public class ElasticSearchIndexCreator {

    @Inject
    private RestHighLevelClient elasticsearchClient;

    @Inject
    private ParameterResolver parameterResolver;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void insertDataIntoElasticsearch(final String indexName, final List<Long> caseRefs) throws IOException {
        final String caseIndex = getIndexName(indexName);
        final List<CaseDataEntity> caseDataEntities = buildCaseDataEntity(
                getIndexName(indexName),
                caseRefs);
        final BulkRequest bulkRequest = buildBulkRequest(caseIndex, caseDataEntities);

        final BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest, DEFAULT);

        assertFalse(bulkResponse.hasFailures());

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

    private BulkRequest buildBulkRequest(final String caseIndex, final List<CaseDataEntity> caseDataEntities) {
        final BulkRequest bulkRequest = new BulkRequest();
        caseDataEntities.forEach(ThrowingConsumer.unchecked(data -> {
            final String value = objectMapper.writeValueAsString(data);
            final IndexRequest indexRequest = new IndexRequest(caseIndex)
                    .source(value, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }));

        return bulkRequest;
    }

    private void refreshIndex(final String caseIndex) throws IOException {
        final RefreshRequest refreshRequest = new RefreshRequest(caseIndex);
        elasticsearchClient.indices().refresh(refreshRequest, DEFAULT);
    }

    public String getIndexName(String caseType) {
        if (!parameterResolver.getGlobalSearchIndexName().equals(caseType)) {
            return String.format("%s_cases", caseType.toLowerCase());
        }
        return caseType;
    }
}
