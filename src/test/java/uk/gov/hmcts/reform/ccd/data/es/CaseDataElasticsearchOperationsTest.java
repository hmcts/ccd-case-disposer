package uk.gov.hmcts.reform.ccd.data.es;

import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkItemResponse.Failure;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.ApplicationParameters;
import uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CaseDataElasticsearchOperationsTest {
    @Mock
    private RestHighLevelClient elasticsearchClient;
    @Mock
    private ApplicationParameters parameters;

    @InjectMocks
    private CaseDataElasticsearchOperations underTest;

    private final SearchResponse searchResponse = mock(SearchResponse.class);
    private final SearchHits searchHits = mock(SearchHits.class);
    private final BulkResponse bulkResponse = mock(BulkResponse.class);

    private static final String CASE_INDEX = "test_case_index";
    private static final String INDEX_TYPE = "_doc";
    private static final Long CASE_REFERENCE = 1902145L;
    private static final String DOCUMENT_ID = UUID.randomUUID().toString();
    private static final SearchHit SEARCH_HIT = new SearchHit(1, DOCUMENT_ID, null, null);
    private static final SearchHit[] SEARCH_HIT_ARRAY = new SearchHit[1];

    @BeforeAll
    static void prepare() {
        SEARCH_HIT_ARRAY[0] = SEARCH_HIT;
    }

    @BeforeEach
    void setup() {
        doReturn(INDEX_TYPE).when(parameters).getCasesIndexType();
        doReturn(1).when(parameters).getElasticsearchRequestTimeout();
    }

    @Test
    void testShouldDeleteByReferenceSuccessfully() throws Exception {
        doReturn(searchResponse).when(elasticsearchClient).search(any(SearchRequest.class), any(RequestOptions.class));
        doReturn(searchHits).when(searchResponse).getHits();
        doReturn(SEARCH_HIT_ARRAY).when(searchHits).getHits();
        doReturn(bulkResponse).when(elasticsearchClient)
            .bulk(any(BulkRequest.class), any(RequestOptions.class));
        doReturn(new BulkItemResponse[0]).when(bulkResponse).getItems();

        underTest.deleteByReference(CASE_INDEX, CASE_REFERENCE);

        verify(elasticsearchClient).search(any(SearchRequest.class), any(RequestOptions.class));
        verify(elasticsearchClient).bulk(any(BulkRequest.class), any(RequestOptions.class));
    }

    @Test
    void testShouldRaiseDeleteFailuresWhenDeleteByReference() throws Exception {
        final BulkItemResponse[] bulkItemResponses = new BulkItemResponse[1];
        final Failure failure = new Failure(CASE_INDEX, INDEX_TYPE, DOCUMENT_ID, new Exception());
        bulkItemResponses[0] = new BulkItemResponse(1, DocWriteRequest.OpType.DELETE, failure);
        doReturn(searchResponse).when(elasticsearchClient).search(any(SearchRequest.class), any(RequestOptions.class));
        doReturn(searchHits).when(searchResponse).getHits();
        doReturn(SEARCH_HIT_ARRAY).when(searchHits).getHits();
        doReturn(bulkResponse).when(elasticsearchClient)
            .bulk(any(BulkRequest.class), any(RequestOptions.class));
        doReturn(bulkItemResponses).when(bulkResponse).getItems();

        assertThatExceptionOfType(ElasticsearchOperationException.class)
            .isThrownBy(() -> underTest.deleteByReference(CASE_INDEX, CASE_REFERENCE))
            .withMessage("Elasticsearch delete operation failed");
        verify(elasticsearchClient).search(any(SearchRequest.class), any(RequestOptions.class));
        verify(elasticsearchClient).bulk(any(BulkRequest.class), any(RequestOptions.class));
    }

    @Test
    void testShouldRaiseUnexpectedOperationFailuresWhenDeleteByReference() throws Exception {
        final String errorMessage = String.format(
            "Unexpected operation: %s, expecting operation type to be of type DELETE",
            DocWriteRequest.OpType.UPDATE
        );
        final BulkItemResponse[] bulkItemResponses = new BulkItemResponse[1];
        bulkItemResponses[0] = new BulkItemResponse(1, DocWriteRequest.OpType.UPDATE, new UpdateResponse());
        doReturn(searchResponse).when(elasticsearchClient).search(any(SearchRequest.class), any(RequestOptions.class));
        doReturn(searchHits).when(searchResponse).getHits();
        doReturn(SEARCH_HIT_ARRAY).when(searchHits).getHits();
        doReturn(bulkResponse).when(elasticsearchClient)
            .bulk(any(BulkRequest.class), any(RequestOptions.class));
        doReturn(bulkItemResponses).when(bulkResponse).getItems();

        assertThatExceptionOfType(ElasticsearchOperationException.class)
            .isThrownBy(() -> underTest.deleteByReference(CASE_INDEX, CASE_REFERENCE))
            .withMessage(errorMessage);
        verify(elasticsearchClient).search(any(SearchRequest.class), any(RequestOptions.class));
        verify(elasticsearchClient).bulk(any(BulkRequest.class), any(RequestOptions.class));
    }

    @Test
    void testShouldRaiseExceptionWhenDeleteByQueryFails() throws Exception {
        doThrow(new IOException()).when(elasticsearchClient)
            .search(any(SearchRequest.class), any(RequestOptions.class));

        assertThatExceptionOfType(ElasticsearchOperationException.class)
            .isThrownBy(() -> underTest.deleteByReference(CASE_INDEX, CASE_REFERENCE));
    }
}
