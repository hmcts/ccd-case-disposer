package uk.gov.hmcts.reform.ccd.data.es;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@Slf4j
public class CaseDataElasticsearchOperations {
    private static final String CASE_REFERENCE_FIELD = "reference";
    private static final String ELASTICSEARCH_DELETE_FAILURE = "Elasticsearch delete operation failed";
    private static final String UNEXPECTED_OPERATION_FAILURE = "Unexpected operation: %s, "
        + "expecting operation type to be of type DELETE";

    private final RestHighLevelClient elasticsearchClient;
    private final ParameterResolver parameterResolver;

    @Inject
    public CaseDataElasticsearchOperations(final RestHighLevelClient elasticsearchClient,
                                           final ParameterResolver parameterResolver) {
        this.elasticsearchClient = elasticsearchClient;
        this.parameterResolver = parameterResolver;
    }

    public void deleteByReference(final String caseIndex, final Long caseReference) {
        try {
            final List<String> documentIds = findCaseByReference(caseIndex, caseReference);

            final BulkRequest request = buildBulkDeleteQueryRequest(caseIndex, documentIds);

            final BulkResponse bulkResponse = elasticsearchClient.bulk(request, RequestOptions.DEFAULT);

            handleBulkResponse(bulkResponse);
        } catch (IOException e) {
            throw new ElasticsearchOperationException(e);
        }
    }

    private List<String> findCaseByReference(final String caseIndex, final Long caseReference) throws IOException {
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            // Option to take a list of caseReferences and use TermsQueryBuilder if needed
            //.query(QueryBuilders.termsQuery(CASE_REFERENCE_FIELD, <list of case references>));
            .query(QueryBuilders.termQuery(CASE_REFERENCE_FIELD, caseReference))
            .from(0)
            .timeout(new TimeValue(parameterResolver.getElasticsearchRequestTimeout(), TimeUnit.SECONDS));

        final SearchRequest searchRequest = new SearchRequest(caseIndex)
            .types(parameterResolver.getCasesIndexType())
            .source(searchSourceBuilder);

        final SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
        return Arrays.stream(searchResponse.getHits().getHits())
            .map(SearchHit::getId)
            .collect(Collectors.toUnmodifiableList());
    }

    private BulkRequest buildBulkDeleteQueryRequest(final String caseIndex, final List<String> documentIds) {
        final BulkRequest bulkRequest = new BulkRequest()
            .setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);

        documentIds.forEach(id -> {
            final DeleteRequest deleteRequest = new DeleteRequest(caseIndex, parameterResolver.getCasesIndexType(), id);
            bulkRequest.add(deleteRequest);
        });

        return bulkRequest;
    }

    private void handleBulkResponse(final BulkResponse bulkResponse) {
        Arrays.stream(bulkResponse.getItems()).forEach(bulkItemResponse -> {
            if (bulkItemResponse.isFailed()) {
                BulkItemResponse.Failure failure = bulkItemResponse.getFailure();

                log.error("{}:: {}", ELASTICSEARCH_DELETE_FAILURE, failure);
                throw new ElasticsearchOperationException(ELASTICSEARCH_DELETE_FAILURE);
            }

            if (bulkItemResponse.getOpType() != DocWriteRequest.OpType.DELETE) {
                final String message = String.format(UNEXPECTED_OPERATION_FAILURE, bulkItemResponse.getOpType());
                log.error(message);
                throw new ElasticsearchOperationException(message);
            }
        });
    }

}
