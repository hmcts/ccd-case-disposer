package uk.gov.hmcts.reform.ccd.data.es;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.ScrollableHitSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.io.IOException;
import java.util.List;

import static java.util.Collections.emptyList;
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
    private ParameterResolver parameterResolver;

    @InjectMocks
    private CaseDataElasticsearchOperations underTest;

    private final BulkByScrollResponse bulkByScrollResponse = mock(BulkByScrollResponse.class);

    private static final String CASE_INDEX = "test_case_index";
    private static final Long CASE_REFERENCE = 1902145L;

    @Test
    void testShouldDeleteByReferenceSuccessfully() throws Exception {
        doReturn(1).when(parameterResolver).getElasticsearchRequestTimeout();
        doReturn(bulkByScrollResponse).when(elasticsearchClient)
            .deleteByQuery(any(DeleteByQueryRequest.class), any(RequestOptions.class));
        doReturn(emptyList()).when(bulkByScrollResponse).getSearchFailures();
        doReturn(emptyList()).when(bulkByScrollResponse).getBulkFailures();

        underTest.deleteByReference(CASE_INDEX, CASE_REFERENCE);

        verify(elasticsearchClient)
            .deleteByQuery(any(DeleteByQueryRequest.class), any(RequestOptions.class));
    }

    @Test
    void testShouldRaiseSearchFailuresWhenDeleteByReference() throws Exception {
        doReturn(1).when(parameterResolver).getElasticsearchRequestTimeout();
        doReturn(bulkByScrollResponse).when(elasticsearchClient)
            .deleteByQuery(any(DeleteByQueryRequest.class), any(RequestOptions.class));
        doReturn(List.of(new ScrollableHitSource.SearchFailure(new Throwable())))
            .when(bulkByScrollResponse).getSearchFailures();
        doReturn(emptyList()).when(bulkByScrollResponse).getBulkFailures();

        assertThatExceptionOfType(ElasticsearchOperationException.class)
            .isThrownBy(() -> underTest.deleteByReference(CASE_INDEX, CASE_REFERENCE))
            .withMessage("Search failures occurred");
        verify(elasticsearchClient)
            .deleteByQuery(any(DeleteByQueryRequest.class), any(RequestOptions.class));
    }

    @Test
    void testShouldRaiseElasticsearchFailuresWhenDeleteByReference() throws Exception {
        doReturn(1).when(parameterResolver).getElasticsearchRequestTimeout();
        doReturn(bulkByScrollResponse).when(elasticsearchClient)
            .deleteByQuery(any(DeleteByQueryRequest.class), any(RequestOptions.class));
        doReturn(emptyList()).when(bulkByScrollResponse).getSearchFailures();
        doReturn(List.of(new BulkItemResponse.Failure(CASE_INDEX, "_doc", "101", new Exception())))
            .when(bulkByScrollResponse).getBulkFailures();

        assertThatExceptionOfType(ElasticsearchOperationException.class)
            .isThrownBy(() -> underTest.deleteByReference(CASE_INDEX, CASE_REFERENCE))
            .withMessage("Elasticsearch operation failures occurred");
        verify(elasticsearchClient)
            .deleteByQuery(any(DeleteByQueryRequest.class), any(RequestOptions.class));
    }

    @Test
    void testShouldRaiseExceptionWhenDeleteByQueryFails() throws Exception {
        doThrow(new IOException()).when(elasticsearchClient)
            .deleteByQuery(any(DeleteByQueryRequest.class), any(RequestOptions.class));

        assertThatExceptionOfType(ElasticsearchOperationException.class)
            .isThrownBy(() -> underTest.deleteByReference(CASE_INDEX, CASE_REFERENCE));
    }
}
