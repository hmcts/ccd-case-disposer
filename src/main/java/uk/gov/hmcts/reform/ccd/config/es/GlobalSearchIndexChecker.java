package uk.gov.hmcts.reform.ccd.config.es;


import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;

import static org.elasticsearch.client.RequestOptions.DEFAULT;

@Named
public class GlobalSearchIndexChecker {
    private final RestHighLevelClient elasticsearchClient;
    private final ParameterResolver parameterResolver;

    @Inject
    public GlobalSearchIndexChecker(final RestHighLevelClient elasticsearchClient,
                                    final ParameterResolver parameterResolver) {
        this.elasticsearchClient = elasticsearchClient;
        this.parameterResolver = parameterResolver;
    }

    public boolean isGlobalSearchExist() {
        try {
            final GetIndexRequest request = new GetIndexRequest(parameterResolver.getGlobalSearchIndexName());
            return elasticsearchClient.indices().exists(request, DEFAULT);
        } catch (IOException e) {
            throw new ElasticsearchOperationException(e);
        }
    }
}
