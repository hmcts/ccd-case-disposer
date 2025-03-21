package uk.gov.hmcts.reform.ccd.service.remote;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.config.es.GlobalSearchIndexChecker;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.io.IOException;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class DisposeElasticsearchRemoteOperation implements DisposeRemoteOperation {
    private static final String CASE_REFERENCE_FIELD = "reference";
    private static final String SEARCH_FAILURES = "Search failures occurred";
    private static final String ELASTICSEARCH_FAILURES = "Elasticsearch operation failures occurred";

    private final ElasticsearchClient elasticsearchClient;
    private final ParameterResolver parameterResolver;
    private final GlobalSearchIndexChecker globalSearchIndexChecker;


    @Override
    public void delete(final CaseData caseData) {
        try {
            final DeleteByQueryRequest caseIndexDeleteRequest = buildDeleteByQueryRequest(
                getIndex(caseData.getCaseType()),
                caseData.getReference()
            );

            deleteByQueryRequest(caseIndexDeleteRequest);

            if (globalSearchIndexChecker.isGlobalSearchExist()) {
                final DeleteByQueryRequest globalSearchIndexDeleteRequest =
                    buildDeleteByQueryRequest(
                        parameterResolver.getGlobalSearchIndexName(),
                        caseData.getReference()
                    );
                deleteByQueryRequest(globalSearchIndexDeleteRequest);
            }
        } catch (final Exception e) {
            throw new ElasticsearchOperationException(e);
        }
    }

    private void deleteByQueryRequest(final DeleteByQueryRequest request) throws IOException {
        try {
            final DeleteByQueryResponse response = elasticsearchClient.deleteByQuery(request);

            if (!isEmpty(response.failures())) {
                throwError(SEARCH_FAILURES, response.failures());
            }
        } catch (ElasticsearchException e) {
            throw new ElasticsearchOperationException(e);
        }
    }

    private DeleteByQueryRequest buildDeleteByQueryRequest(final String caseIndex, final Long caseReference) {
        return DeleteByQueryRequest.of(b -> b
            .index(caseIndex)
            .query(q -> q
                .term(t -> t
                    .field(CASE_REFERENCE_FIELD)
                    .value(caseReference)
                )
            )
            .refresh(true)
        );
    }

    private <T> void throwError(final String message, final List<T> list) {
        log.error("{}:: {}", message, list);
        throw new ElasticsearchOperationException(message);
    }

    private String getIndex(final String caseType) {
        return String.format(parameterResolver.getCasesIndexNamePattern(), caseType).toLowerCase();
    }

}
