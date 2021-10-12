package uk.gov.hmcts.reform.ccd.data.es;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.ScrollableHitSource;
import uk.gov.hmcts.reform.ccd.ApplicationParameters;
import uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException;

import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@Slf4j
public class CaseDataElasticsearchOperations {
    private static final String CASE_REFERENCE_FIELD = "reference";
    private static final String SEARCH_FAILURES = "Search failures occurred";
    private static final String ELASTICSEARCH_FAILURES = "Elasticsearch operation failures occurred";

    private final RestHighLevelClient elasticsearchClient;
    private final ApplicationParameters parameters;

    @Inject
    public CaseDataElasticsearchOperations(final RestHighLevelClient elasticsearchClient,
                                           final ApplicationParameters parameters) {
        this.elasticsearchClient = elasticsearchClient;
        this.parameters = parameters;
    }

    public void deleteByReference(final String caseIndex, final Long caseReference) {
        final DeleteByQueryRequest request = buildDeleteByQueryRequest(caseIndex, caseReference);

        try {
            final RequestOptions requestOptions = buildRequestOptions();
            final BulkByScrollResponse bulkResponse = elasticsearchClient.deleteByQuery(request, requestOptions);

            final List<ScrollableHitSource.SearchFailure> searchFailures = bulkResponse.getSearchFailures();
            final List<BulkItemResponse.Failure> bulkFailures = bulkResponse.getBulkFailures();

            if (isPresent(searchFailures)) {
                throwError(SEARCH_FAILURES, searchFailures);
            }
            if (isPresent(bulkFailures)) {
                throwError(ELASTICSEARCH_FAILURES, bulkFailures);
            }
        } catch (IOException e) {
            throw new ElasticsearchOperationException(e);
        }
    }

    private DeleteByQueryRequest buildDeleteByQueryRequest(final String caseIndex, final Long caseReference) {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(caseIndex);
        deleteByQueryRequest.setConflicts("proceed");
        // Option to take a list of caseReferences and use TermsQueryBuilder if needed
        deleteByQueryRequest.setQuery(new TermQueryBuilder(CASE_REFERENCE_FIELD, caseReference));
        deleteByQueryRequest.setRefresh(true);

        return deleteByQueryRequest;
    }

    private RequestOptions buildRequestOptions() {
        final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(parameters.getElasticsearchRequestTimeout())
            .build();
        return RequestOptions.DEFAULT.toBuilder()
            .setRequestConfig(requestConfig)
            .build();
    }

    private <T> boolean isPresent(List<T> list) {
        return !list.isEmpty();
    }

    private <T> void throwError(final String message, final List<T> list) {
        log.error("{}:: {}", message, list);
        throw new ElasticsearchOperationException(message);
    }
}
