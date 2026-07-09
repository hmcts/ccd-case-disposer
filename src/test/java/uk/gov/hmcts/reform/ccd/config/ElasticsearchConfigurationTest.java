package uk.gov.hmcts.reform.ccd.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.CloseResource")
class ElasticsearchConfigurationTest {

    @Mock
    private ParameterResolver parameterResolver;

    @Mock
    private ElasticsearchTransport transport;


    @Test
    void shouldThrowIllegalArgumentExceptionWhenHostIsInvalid() {
        doReturn(List.of("http://:9200")).when(parameterResolver).getElasticsearchHosts();

        ElasticsearchConfiguration configuration = new ElasticsearchConfiguration();

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> configuration.restClient(parameterResolver))
            .withMessageContaining("Invalid Elasticsearch host");
    }

    @Test
    void shouldCreateElasticsearchTransport() {
        Rest5Client restClient = mock(Rest5Client.class);
        ElasticsearchConfiguration configuration = new ElasticsearchConfiguration();
        ElasticsearchTransport result = configuration.elasticsearchTransport(restClient);
        assertThat(result).isInstanceOf(Rest5ClientTransport.class);
    }

    @Test
    void shouldCreateElasticsearchClient() {
        ElasticsearchConfiguration configuration = new ElasticsearchConfiguration();
        ElasticsearchClient result = configuration.elasticsearchClient(transport);
        assertThat(result).isNotNull();
    }
}
