package uk.gov.hmcts.reform.ccd.config.es;

import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalSearchIndexCheckerTest {

    @Mock
    private RestHighLevelClient elasticsearchClient;
    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private GlobalSearchIndexChecker globalSearchIndexChecker;


    @Test
    void shouldReturnTrueWhenGlobalSearchExist() throws IOException {
        doReturn("global_search").when(parameterResolver).getGlobalSearchIndexName();
        final IndicesClient indices = mock(IndicesClient.class);
        when(elasticsearchClient.indices()).thenReturn(indices);

        doReturn(true).when(indices).exists(any(GetIndexRequest.class), any());
        assertThat(globalSearchIndexChecker.isGlobalSearchExist()).isEqualTo(true);
    }

    @Test
    void shouldReturnFalseWhenGlobalSearchExist() throws IOException {
        doReturn("global_search").when(parameterResolver).getGlobalSearchIndexName();
        final IndicesClient indices = mock(IndicesClient.class);
        when(elasticsearchClient.indices()).thenReturn(indices);

        doReturn(false).when(indices).exists(any(GetIndexRequest.class), any());
        assertThat(globalSearchIndexChecker.isGlobalSearchExist()).isEqualTo(false);
    }


    @Test
    void shouldThrowExceptionWhenGlobalSearchExist() throws Exception {

        doReturn("global_search").when(parameterResolver).getGlobalSearchIndexName();
        final IndicesClient indices = mock(IndicesClient.class);
        when(elasticsearchClient.indices()).thenReturn(indices);

        doThrow(new IOException()).when(indices).exists(any(GetIndexRequest.class), any());

        assertThatExceptionOfType(ElasticsearchOperationException.class)
                .isThrownBy(() -> globalSearchIndexChecker.isGlobalSearchExist());
    }
}