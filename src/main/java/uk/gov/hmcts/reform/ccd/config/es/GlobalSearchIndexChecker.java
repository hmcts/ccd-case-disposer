package uk.gov.hmcts.reform.ccd.config.es;


import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.io.IOException;

import static org.elasticsearch.client.RequestOptions.DEFAULT;

@Named
@RequiredArgsConstructor
public class GlobalSearchIndexChecker {
    private final RestHighLevelClient elasticsearchClient;
    private final ParameterResolver parameterResolver;

    public boolean isGlobalSearchExist() {
        try {
            final GetIndexRequest request = new GetIndexRequest(parameterResolver.getGlobalSearchIndexName());
            return elasticsearchClient.indices().exists(request, DEFAULT);
        } catch (IOException e) {
            throw new ElasticsearchOperationException(e);
        }
    }
}
