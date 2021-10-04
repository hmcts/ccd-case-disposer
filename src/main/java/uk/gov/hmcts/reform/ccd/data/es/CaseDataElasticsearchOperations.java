package uk.gov.hmcts.reform.ccd.data.es;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ByQueryResponse;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@Slf4j
public class CaseDataElasticsearchOperations {
    private static final String CASE_REFERENCE_FIELD = "reference";
    private static final String SEARCH_FAILURES = "Search failures occurred";
    private static final String ELASTICSEARCH_FAILURES = "Elasticsearch failures occurred";

    private final ElasticsearchOperations elasticsearchOperations;

    @Inject
    public CaseDataElasticsearchOperations(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public void deleteByReference(final String caseIndex, final Long caseReference) {
        final QueryBuilder queryBuilder = QueryBuilders.matchQuery(CASE_REFERENCE_FIELD, caseReference);
        final Query searchQuery = new NativeSearchQueryBuilder()
            .withQuery(queryBuilder)
            .build();
        final ByQueryResponse response = elasticsearchOperations.delete(
            searchQuery,
            CaseDataEntity.class,
            IndexCoordinates.of(caseIndex)
        );
        if (isPresent(response.getSearchFailures())) {
            throwError(SEARCH_FAILURES, response.getSearchFailures());
        }
        if (isPresent(response.getFailures())) {
            throwError(ELASTICSEARCH_FAILURES, response.getFailures());
        }
    }

    private <T> boolean isPresent(List<T> list) {
        return !list.isEmpty();
    }

    private <T> void throwError(final String message, final List<T> list) {
        log.error("{}:: {}", message, list);
        throw new ElasticsearchOperationException(message);
    }
}
