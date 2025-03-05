package uk.gov.hmcts.reform.ccd.config.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.endpoints.BooleanResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.function.Function;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked"})
@ExtendWith(MockitoExtension.class)
class GlobalSearchIndexCheckerTest {

    @Mock
    private ElasticsearchClient elasticsearchClient;
    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private GlobalSearchIndexChecker globalSearchIndexChecker;

    @Test
    void shouldReturnTrueWhenGlobalSearchExist() throws IOException {
        doReturn("global_search").when(parameterResolver).getGlobalSearchIndexName();
        BooleanResponse booleanResponse = mock(BooleanResponse.class);
        when(booleanResponse.value()).thenReturn(true);
        //when(elasticsearchClient.indices().exists(any())).thenReturn(booleanResponse);
        when(elasticsearchClient.indices().exists(any(Function.class))).thenReturn(booleanResponse);

        assertThat(globalSearchIndexChecker.isGlobalSearchExist()).isEqualTo(true);
    }

    @Test
    void shouldReturnFalseWhenGlobalSearchExist() throws IOException {
        doReturn("global_search").when(parameterResolver).getGlobalSearchIndexName();
        BooleanResponse booleanResponse = mock(BooleanResponse.class);
        when(booleanResponse.value()).thenReturn(false);

        when(elasticsearchClient.indices().exists(any(Function.class))).thenReturn(booleanResponse);
        assertThat(globalSearchIndexChecker.isGlobalSearchExist()).isEqualTo(false);
    }


    @Test
    void shouldThrowExceptionWhenGlobalSearchExist() throws IOException {
        doReturn("global_search").when(parameterResolver).getGlobalSearchIndexName();
        when(elasticsearchClient.indices().exists(any(Function.class))).thenThrow(new IOException());

        assertThatExceptionOfType(ElasticsearchOperationException.class)
            .isThrownBy(() -> globalSearchIndexChecker.isGlobalSearchExist());
    }
}
