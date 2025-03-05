package uk.gov.hmcts.reform.ccd.service.remote;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import co.elastic.clients.elasticsearch._types.BulkIndexByScrollFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.config.es.GlobalSearchIndexChecker;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DisposeElasticsearchRemoteOperationTest {
    private static final String CASE_INDEX = "test_case_index";
    private static final String GLOBAL_SEARCH_INDEX = "global_search";
    private static final Long CASE_REFERENCE = 1902145L;
    //private final BulkByScrollResponse bulkByScrollResponse = mock(BulkByScrollResponse.class);
    @Mock
    private ElasticsearchClient elasticsearchClient;
    @Mock
    private ParameterResolver parameterResolver;
    @Mock
    private GlobalSearchIndexChecker globalSearchIndexChecker;
    @InjectMocks
    private DisposeElasticsearchRemoteOperation underTest;

    final CaseData caseData = CaseData.builder().reference(CASE_REFERENCE).build();

    @BeforeEach
    void prepare() {
        doReturn(CASE_INDEX).when(parameterResolver).getCasesIndexNamePattern();
        doReturn(1).when(parameterResolver).getElasticsearchRequestTimeout();
    }

    @Test
    void testShouldDeleteByReferenceSuccessfully() throws Exception {
        doReturn(GLOBAL_SEARCH_INDEX).when(parameterResolver).getGlobalSearchIndexName();
        DeleteByQueryResponse deleteByQueryResponse = mock(DeleteByQueryResponse.class);
        doReturn(deleteByQueryResponse).when(elasticsearchClient)
            .deleteByQuery(any(DeleteByQueryRequest.class));
        doReturn(emptyList()).when(deleteByQueryResponse).failures();

        doReturn(true).when(globalSearchIndexChecker).isGlobalSearchExist();

        underTest.delete(caseData);

        verify(elasticsearchClient, times(2))
            .deleteByQuery(any(DeleteByQueryRequest.class));
    }

    @Test
    void testShouldIgnoreDeleteByQueryRequestIfGlobalSearchNotDefined() throws Exception {
        DeleteByQueryResponse deleteByQueryResponse = mock(DeleteByQueryResponse.class);
        doReturn(deleteByQueryResponse).when(elasticsearchClient)
            .deleteByQuery(any(DeleteByQueryRequest.class));
        doReturn(emptyList()).when(deleteByQueryResponse).failures();

        doReturn(false).when(globalSearchIndexChecker).isGlobalSearchExist();

        underTest.delete(caseData);

        verify(elasticsearchClient, times(1))
            .deleteByQuery(any(DeleteByQueryRequest.class));
    }

    @Test
    void testShouldRaiseSearchFailuresWhenDeleteByReference() throws Exception {
        DeleteByQueryResponse deleteByQueryResponse = mock(DeleteByQueryResponse.class);
        doReturn(deleteByQueryResponse).when(elasticsearchClient)
            .deleteByQuery(any(DeleteByQueryRequest.class));
        BulkIndexByScrollFailure failure = mock(BulkIndexByScrollFailure.class);
        doReturn(List.of(failure)).when(deleteByQueryResponse).failures();

        assertThatExceptionOfType(ElasticsearchOperationException.class)
            .isThrownBy(() -> underTest.delete(caseData))
            .withMessage("uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException: "
                             + "Search failures occurred");
        verify(elasticsearchClient)
            .deleteByQuery(any(DeleteByQueryRequest.class));
    }

    @Test
    void testShouldRaiseElasticsearchFailuresWhenDeleteByReference() throws Exception {
        DeleteByQueryResponse deleteByQueryResponse = mock(DeleteByQueryResponse.class);
        doReturn(deleteByQueryResponse).when(elasticsearchClient)
            .deleteByQuery(any(DeleteByQueryRequest.class));
        doReturn(emptyList()).when(deleteByQueryResponse).failures();
        BulkIndexByScrollFailure failure = mock(BulkIndexByScrollFailure.class);
        doReturn(List.of(failure)).when(deleteByQueryResponse).failures();

        assertThatExceptionOfType(ElasticsearchOperationException.class)
            .isThrownBy(() -> underTest.delete(caseData))
            .withMessage("uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException: "
                             + "Elasticsearch operation failures occurred");
        verify(elasticsearchClient)
            .deleteByQuery(any(DeleteByQueryRequest.class));
    }

    @Test
    void testShouldRaiseExceptionWhenDeleteByQueryFails() throws Exception {
        doThrow(new IOException()).when(elasticsearchClient)
            .deleteByQuery(any(DeleteByQueryRequest.class));

        assertThatExceptionOfType(ElasticsearchOperationException.class)
            .isThrownBy(() -> underTest.delete(caseData));
    }
}
