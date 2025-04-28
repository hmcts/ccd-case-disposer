package uk.gov.hmcts.reform.ccd.config.es;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.io.IOException;

@Named
@RequiredArgsConstructor
public class GlobalSearchIndexChecker {
    private final ElasticsearchClient elasticsearchClient;
    private final ParameterResolver parameterResolver;

    public boolean isGlobalSearchExist() {
        try {
            final String indexName = parameterResolver.getGlobalSearchIndexName();
            BooleanResponse response = elasticsearchClient.indices().exists(b -> b.index(indexName));
            return response.value();
        } catch (IOException e) {
            throw new ElasticsearchOperationException(e);
        }
    }
}
