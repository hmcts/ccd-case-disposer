package uk.gov.hmcts.reform.ccd.service.remote;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.ScrollableHitSource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.config.es.GlobalSearchIndexChecker;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.io.IOException;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class DisposeElasticsearchRemoteOperation implements DisposeRemoteOperation {
    private static final String CASE_REFERENCE_FIELD = "reference";
    private static final String SEARCH_FAILURES = "Search failures occurred";
    private static final String ELASTICSEARCH_FAILURES = "Elasticsearch operation failures occurred";

    private final RestHighLevelClient elasticsearchClient;
    private final ParameterResolver parameterResolver;
    private final GlobalSearchIndexChecker globalSearchIndexChecker;


    @Override
    public void delete(final CaseData caseData) {
        final DeleteByQueryRequest caseIndexDeleteRequest = buildDeleteByQueryRequest(getIndex(caseData.getCaseType()),
                caseData.getReference());

        deleteByQueryRequest(caseIndexDeleteRequest);

        if (globalSearchIndexChecker.isGlobalSearchExist()) {
            final DeleteByQueryRequest globalSearchIndexDeleteRequest =
                    buildDeleteByQueryRequest(parameterResolver.getGlobalSearchIndexName(),
                            caseData.getReference());
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
                throwError(ELASTICSEARCH_FAILURES, bulkFailures);
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

    private String getIndex(final String caseType) {
        return String.format(parameterResolver.getCasesIndexNamePattern(), caseType).toLowerCase();
    }
}
