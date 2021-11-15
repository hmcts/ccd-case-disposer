package uk.gov.hmcts.reform.ccd;

import com.pivovarit.function.ThrowingPredicate;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.ccd.config.ElasticsearchConfiguration;
import uk.gov.hmcts.reform.ccd.config.TestApplicationConfiguration;
import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventEntity;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;

import static org.awaitility.Awaitility.with;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {TestApplicationConfiguration.class, ElasticsearchConfiguration.class})
@ActiveProfiles("functional")
@Sql("/test_data.sql")
public class CaseDeletionServiceFunctionalTest {

    private static final String CASE_REFERENCE_FIELD = "reference";

    @Autowired
    private ApplicationExecutor executor;

    @Inject
    private RestHighLevelClient elasticsearchClient;

    @Inject
    private List<String> testCaseTypes;

    @Inject
    private CaseDataRepository caseDataRepository;

    @Inject
    private CaseEventRepository caseEventRepository;

    @BeforeAll
    static void setup() {
        Awaitility.setDefaultPollInterval(10, TimeUnit.SECONDS);
        Awaitility.setDefaultPollDelay(Duration.TEN_SECONDS);
        Awaitility.setDefaultTimeout(Duration.FIVE_MINUTES);
    }


    @Test
    public void caseShouldGetDeletedAfterServiceExecution() throws Exception {
        // GIVEN
        Optional<CaseDataEntity> caseDataToDelete = caseDataRepository.findById(1L);
        assertTrue(caseDataToDelete.isPresent());

        Optional<CaseEventEntity> caseEventToDelete = caseEventRepository.findById(1L);
        assertTrue(caseEventToDelete.isPresent());

        with()
            .pollDelay(Duration.TEN_SECONDS)
            .pollInterval(Duration.TEN_SECONDS)
            .await()
                .until(this::checkIndexesPrepared);

        // WHEN
        executor.execute();

        // THEN
        Optional<CaseDataEntity> deletedCaseData = caseDataRepository.findById(caseDataToDelete.get().getId());
        Optional<CaseEventEntity> deletedCaseEvent = caseEventRepository.findById(caseEventToDelete.get().getId());

        assertTrue(deletedCaseData.isEmpty());
        assertTrue(deletedCaseEvent.isEmpty());

        List<String> caseReferences = findCaseByReference(
            getIndex(caseDataToDelete.get().getCaseType()),
            caseDataToDelete.get().getReference()
        );

        assertFalse(caseReferences.contains(caseDataToDelete.get().getReference()));
    }

    private boolean doesIndexExist(final String index) throws IOException {
        refreshIndex(index);
        final GetIndexRequest request = new GetIndexRequest()
            .indices(index);
        return elasticsearchClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    private boolean checkIndexesPrepared() {
        return testCaseTypes.stream().anyMatch(ThrowingPredicate.unchecked(caseType -> {
            final String index = getIndex(caseType);
            return doesIndexExist(index);
        }));
    }

    private String getIndex(String caseType) {
        return String.format("%s_cases", caseType.toLowerCase());
    }

    private void refreshIndex(final String index) throws IOException {
        RefreshRequest request = new RefreshRequest(index);
        elasticsearchClient.indices().refresh(request, RequestOptions.DEFAULT);
    }

    private List<String> findCaseByReference(final String caseIndex, final Long caseReference) throws IOException {
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .query(QueryBuilders.termQuery(CASE_REFERENCE_FIELD, caseReference))
            .from(0);

        final SearchRequest searchRequest = new SearchRequest(caseIndex)
            .types("_doc")
            .source(searchSourceBuilder);

        final SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
        return Arrays.stream(searchResponse.getHits().getHits())
            .map(SearchHit::getId)
            .collect(Collectors.toUnmodifiableList());
    }

}
