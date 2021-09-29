package uk.gov.hmcts.reform.ccd.data.es;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import javax.inject.Inject;

public class CaseDataSearchOperations {

    private final ElasticsearchOperations elasticsearchOperations;

    @Inject
    public CaseDataSearchOperations(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public void deleteCaseDataByReference(final String caseIndex, final Long caseReference) {
        final QueryBuilder queryBuilder = QueryBuilders.matchQuery("reference", caseReference);
        final Query searchQuery = new NativeSearchQueryBuilder()
            .withQuery(queryBuilder)
            .build();
        elasticsearchOperations.delete(
            searchQuery,
            CaseDataEntity.class,
            IndexCoordinates.of(caseIndex)
        );
    }
}
