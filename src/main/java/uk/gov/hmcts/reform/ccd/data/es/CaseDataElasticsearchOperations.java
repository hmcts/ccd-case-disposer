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
import uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import static org.springframework.util.CollectionUtils.isEmpty;

@Named
@Slf4j
public class CaseDataElasticsearchOperations {
    private static final String CASE_REFERENCE_FIELD = "reference";
    private static final String SEARCH_FAILURES = "Search failures occurred";
    private static final String ELASTIC_SEARCH_FAILURES = "Elasticsearch operation failures occurred";

    private final RestHighLevelClient elasticsearchClient;
    private final ParameterResolver parameterResolver;

    @Inject
    public CaseDataElasticsearchOperations(final RestHighLevelClient elasticsearchClient,
                                           final ParameterResolver parameterResolver) {
        this.elasticsearchClient = elasticsearchClient;
        this.parameterResolver = parameterResolver;
    }

    public void deleteByReference(final String caseIndex, final Long caseReference) {
        final DeleteByQueryRequest caseIndexDeleteRequest = buildDeleteByQueryRequest(caseIndex,
                caseReference);

        deleteByQueryRequest(caseIndexDeleteRequest);

        if (parameterResolver.isGlobalSearchEnabled()) {
            final DeleteByQueryRequest globalSearchIndexDeleteRequest =
                    buildDeleteByQueryRequest(parameterResolver.getGlobalSearchIndexName(),
                            caseReference);
            deleteByQueryRequest(globalSearchIndexDeleteRequest);
        }
    }

    private void deleteByQueryRequest(final DeleteByQueryRequest request) {
        try {
            final RequestOptions requestOptions = buildRequestOptions();
            final BulkByScrollResponse bulkResponse = elasticsearchClient.deleteByQuery(request, requestOptions);

            final List<ScrollableHitSource.SearchFailure> searchFailures = bulkResponse.getSearchFailures();
            final List<BulkItemResponse.Failure> bulkFailures = bulkResponse.getBulkFailures();

            if (!isEmpty(searchFailures)) {
                throwError(SEARCH_FAILURES, searchFailures);
            }
            if (!isEmpty(bulkFailures)) {
                throwError(ELASTIC_SEARCH_FAILURES, bulkFailures);
            }
        } catch (final IOException e) {
            throw new ElasticsearchOperationException(e);
        }
    }

    private DeleteByQueryRequest buildDeleteByQueryRequest(final String caseIndex, final Long caseReference) {
        final DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(caseIndex);
        deleteByQueryRequest.setConflicts("proceed");
        // Option to take a list of caseReferences and use TermsQueryBuilder if needed
        deleteByQueryRequest.setQuery(new TermQueryBuilder(CASE_REFERENCE_FIELD, caseReference));
        deleteByQueryRequest.setRefresh(true);

        return deleteByQueryRequest;
    }

    private RequestOptions buildRequestOptions() {
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(parameterResolver.getElasticsearchRequestTimeout())
                .build();
        return RequestOptions.DEFAULT.toBuilder()
                .setRequestConfig(requestConfig)
                .build();
    }

    private <T> void throwError(final String message, final List<T> list) {
        log.error("{}:: {}", message, list);
        throw new ElasticsearchOperationException(message);
    }
}
