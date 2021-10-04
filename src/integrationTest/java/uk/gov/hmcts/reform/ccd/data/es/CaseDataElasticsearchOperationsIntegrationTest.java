package uk.gov.hmcts.reform.ccd.data.es;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import uk.gov.hmcts.reform.ccd.ApplicationParameters;
import uk.gov.hmcts.reform.ccd.config.ApplicationConfiguration;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.fixture.CaseDataEntityBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.INDEX_NAME_PATTERN;

@SpringBootTest(classes = {
    ApplicationParameters.class,
    ApplicationConfiguration.class,
    CaseDataElasticsearchOperations.class}
)
class CaseDataElasticsearchOperationsIntegrationTest extends TestElasticsearchFixture {
    private final List<String> caseTypes = List.of("aa", "bb");

    @Inject
    private ElasticsearchOperations elasticsearchOperations;

    @Inject
    private CaseDataElasticsearchOperations underTest;

    @BeforeEach
    void prepare() {
        caseTypes.forEach(x -> {
            final String caseIndex = String.format(INDEX_NAME_PATTERN, x);
            final List<CaseDataEntity> caseDataEntities = buildData(x);

            final List<IndexQuery> queries = caseDataEntities.stream()
                .map(caseDataEntity -> new IndexQueryBuilder()
                    .withId(caseDataEntity.getId().toString())
                    .withObject(caseDataEntity).build())
                .collect(Collectors.toUnmodifiableList());

            elasticsearchOperations.bulkIndex(queries, IndexCoordinates.of(caseIndex));

            elasticsearchOperations.indexOps(IndexCoordinates.of(caseIndex)).refresh();
        });
    }

    private List<CaseDataEntity> buildData(final String caseType) {
        return IntStream.rangeClosed(1, 5)
            .mapToObj(number -> new CaseDataEntityBuilder((long) number)
                .withReference((long) number)
                .withCaseType(caseType)
                .build())
            .collect(Collectors.toUnmodifiableList());
    }

    @Test
    void testDeleteByReference() {
        underTest.deleteByReference("aa_cases", 3L);

        final QueryBuilder queryBuilder = QueryBuilders.matchQuery("reference", 3);
        final Query searchQuery = new NativeSearchQueryBuilder()
            .withQuery(queryBuilder)
            .build();
        final SearchHits<CaseDataEntity> searchHits = elasticsearchOperations
            .search(searchQuery, CaseDataEntity.class, IndexCoordinates.of("aa_cases"));

        assertThat(searchHits).isEmpty();
        assertThatCaseTypeBBsAreAllPresent("aa_cases", 4);
        assertThatCaseTypeBBsAreAllPresent("bb_cases", 5);
    }

    private void assertThatCaseTypeBBsAreAllPresent(final String index, final int count) {
        final Query searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).build();
        final SearchHits<CaseDataEntity> searchHits = elasticsearchOperations
            .search(searchQuery, CaseDataEntity.class, IndexCoordinates.of(index));

        assertThat(searchHits)
            .isNotEmpty()
            .hasSize(count);
    }
}
