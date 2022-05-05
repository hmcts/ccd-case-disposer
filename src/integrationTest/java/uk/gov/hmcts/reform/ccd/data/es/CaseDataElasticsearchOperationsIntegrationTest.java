package uk.gov.hmcts.reform.ccd.data.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pivovarit.function.ThrowingConsumer;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.config.ApplicationConfiguration;
import uk.gov.hmcts.reform.ccd.config.ElasticsearchConfiguration;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.fixture.CaseDataEntityBuilder;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.INDEX_NAME_PATTERN;

@SpringBootTest(classes = {
    ParameterResolver.class,
    ApplicationConfiguration.class,
    ElasticsearchConfiguration.class,
    CaseDataElasticsearchOperations.class}
)
class CaseDataElasticsearchOperationsIntegrationTest extends TestElasticsearchFixture {
    private final List<String> caseTypes = List.of("aa", "bb");

    @Inject
    private RestHighLevelClient elasticsearchClient;

    @Inject
    private CaseDataElasticsearchOperations underTest;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void prepare() {
        caseTypes.forEach(ThrowingConsumer.unchecked(caseType -> {
            final String caseIndex = String.format(INDEX_NAME_PATTERN, caseType);
            final List<CaseDataEntity> caseDataEntities = buildData(caseType);
            final BulkRequest bulkRequest = buildBulkRequest(caseIndex, caseDataEntities);

            final BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest, RequestOptions.DEFAULT);

            assertFalse(bulkResponse.hasFailures());

            refreshIndex(caseIndex);
        }));
    }

    private List<CaseDataEntity> buildData(final String caseType) {
        return IntStream.rangeClosed(1, 5)
            .mapToObj(number -> new CaseDataEntityBuilder((long) number)
                .withReference((long) number)
                .withCaseType(caseType)
                .build())
            .collect(Collectors.toUnmodifiableList());
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
        elasticsearchClient.indices().refresh(refreshRequest, RequestOptions.DEFAULT);
    }

    @Test
    void testDeleteByReference() throws Exception {
        // GIVEN
        final String aaCases = "aa_cases";

        final List<Long> preDeleteCaseReferences = searchIndex(aaCases);
        assertThat(preDeleteCaseReferences)
            .isNotEmpty()
            .hasSameElementsAs(List.of(1L, 2L, 3L, 4L, 5L));

        // WHEN
        underTest.deleteByReference(aaCases, 3L);

        // THEN
        final List<Long> postDeleteCaseReferences = searchIndex(aaCases);
        assertThat(postDeleteCaseReferences)
            .isNotEmpty()
            .hasSameElementsAs(List.of(1L, 2L, 4L, 5L));

        final List<Long> bbCaseReferences = searchIndex("bb_cases");
        assertThat(bbCaseReferences)
            .isNotEmpty()
            .hasSameElementsAs(List.of(1L, 2L, 3L, 4L, 5L));
    }

    private List<Long> searchIndex(final String index) throws IOException {
        final SearchRequest searchRequest = new SearchRequest(index);
        final SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
        final SearchHit[] searchHits = response.getHits().getHits();

        return Arrays.stream(searchHits)
            .map(hit -> {
                final Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                return Long.parseLong(sourceAsMap.get("reference").toString());
            })
            .collect(Collectors.toUnmodifiableList());
    }

}
