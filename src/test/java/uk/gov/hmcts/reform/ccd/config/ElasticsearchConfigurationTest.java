package uk.gov.hmcts.reform.ccd.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ElasticsearchConfigurationTest {

    @Mock
    private ParameterResolver parameterResolver;

    @Test
    void shouldThrowIllegalArgumentExceptionWhenHostIsInvalid() {
        doReturn(List.of("http://:9200")).when(parameterResolver).getElasticsearchHosts();

        ElasticsearchConfiguration configuration = new ElasticsearchConfiguration(parameterResolver);

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(configuration::init)
            .withMessageContaining("Invalid Elasticsearch host");
    }
}
