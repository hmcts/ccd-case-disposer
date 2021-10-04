package uk.gov.hmcts.reform.ccd.data.es;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ByQueryResponse;
import org.springframework.data.elasticsearch.core.query.Query;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.exception.ElasticsearchOperationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CaseDataElasticsearchOperationsTest {
    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private CaseDataElasticsearchOperations underTest;

    private static final String CASE_INDEX = "test_case_index";
    private static final Long CASE_REFERENCE = 1902145L;

    @Test
    void testShouldDeleteByReferenceSuccessfully() {
        final ByQueryResponse response = ByQueryResponse.builder()
            .build();
        doReturn(response).when(elasticsearchOperations)
            .delete(any(Query.class), eq(CaseDataEntity.class), any(IndexCoordinates.class));

        underTest.deleteByReference(CASE_INDEX, CASE_REFERENCE);

        verify(elasticsearchOperations)
            .delete(any(Query.class), eq(CaseDataEntity.class), eq(IndexCoordinates.of(CASE_INDEX)));
    }

    @Test
    void testShouldRaiseSearchFailuresWhenDeleteByReference() {
        final ByQueryResponse response = ByQueryResponse.builder()
            .withSearchFailure(List.of(ByQueryResponse.SearchFailure.builder().withIndex(CASE_INDEX).build()))
            .build();
        doReturn(response).when(elasticsearchOperations)
            .delete(any(Query.class), eq(CaseDataEntity.class), any(IndexCoordinates.class));

        assertThatExceptionOfType(ElasticsearchOperationException.class)
            .isThrownBy(() -> underTest.deleteByReference(CASE_INDEX, CASE_REFERENCE))
            .withMessage("Search failures occurred");
        verify(elasticsearchOperations)
            .delete(any(Query.class), eq(CaseDataEntity.class), eq(IndexCoordinates.of(CASE_INDEX)));
    }

    @Test
    void testShouldRaiseElasticsearchFailuresWhenDeleteByReference() {
        final ByQueryResponse response = ByQueryResponse.builder()
            .withFailures(List.of(ByQueryResponse.Failure.builder().withIndex(CASE_INDEX).build()))
            .build();
        doReturn(response).when(elasticsearchOperations)
            .delete(any(Query.class), eq(CaseDataEntity.class), any(IndexCoordinates.class));

        assertThatExceptionOfType(ElasticsearchOperationException.class)
            .isThrownBy(() -> underTest.deleteByReference(CASE_INDEX, CASE_REFERENCE))
            .withMessage("Elasticsearch failures occurred");
        verify(elasticsearchOperations)
            .delete(any(Query.class), eq(CaseDataEntity.class), eq(IndexCoordinates.of(CASE_INDEX)));
    }
}
