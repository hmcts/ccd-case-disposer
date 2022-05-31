package uk.gov.hmcts.reform.ccd.parameter;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DefaultParameterResolverTest {

    private static final String ELASTIC_SEARCH_HOSTS = "elasticsearchHosts";
    private static final String ELASTIC_SEARCH_REQUEST_TIMEOUT = "elasticsearchRequestTimeout";
    private static final String CASE_INDEX_NAME_PATTERN = "casesIndexNamePattern";
    private static final String CASES_INDEX_TYPE = "casesIndexType";

    private static final String DELETABLE_CASE_TYPES = "deletableCaseTypes";

    private static final String DOCUMENT_STORE_HOST = "documentStoreHost";

    protected DefaultParameterResolver defaultParameterResolver = new DefaultParameterResolver();

    @Before
    @BeforeEach
    public void initMock() {

        ReflectionTestUtils.setField(defaultParameterResolver,
                                     ELASTIC_SEARCH_HOSTS, Arrays.asList("http://localhost:9200"));
        ReflectionTestUtils.setField(defaultParameterResolver,
                                     ELASTIC_SEARCH_REQUEST_TIMEOUT, 6000);
        ReflectionTestUtils.setField(defaultParameterResolver,
                                     CASE_INDEX_NAME_PATTERN, "%s_cases");
        ReflectionTestUtils.setField(defaultParameterResolver,
                                     CASES_INDEX_TYPE, "_doc");

        ReflectionTestUtils.setField(defaultParameterResolver,
                                     DELETABLE_CASE_TYPES, Arrays.asList("Case_Type_01"));

        ReflectionTestUtils.setField(defaultParameterResolver,
                DOCUMENT_STORE_HOST, "http://localhost:4603");
    }

    @Test
    @DisplayName("should get correct value for getElasticsearchHosts")
    void shouldGetCorrectValueForGetElasticsearchHosts() {
        assertThat(defaultParameterResolver.getElasticsearchHosts(),
                   is(equalTo(Arrays.asList("http://localhost:9200"))));
    }

    @Test
    @DisplayName("should get correct value for getElasticsearchRequestTimeout")
    void shouldGetCorrectValueForGetElasticsearchRequestTimeout() {
        assertThat(defaultParameterResolver.getElasticsearchRequestTimeout(),
                   is(equalTo(6000)));
    }

    @Test
    @DisplayName("should get correct value for getCasesIndexNamePattern")
    void shouldGetCorrectValueForGetCasesIndexNamePattern() {
        assertThat(defaultParameterResolver.getCasesIndexNamePattern(),
                   is(equalTo("%s_cases")));
    }

    @Test
    @DisplayName("should get correct value for getCasesIndexType")
    void shouldGetCorrectValueForGetCasesIndexType() {
        assertThat(defaultParameterResolver.getCasesIndexType(),
                   is(equalTo("_doc")));
    }

    @Test
    @DisplayName("should get correct value for getDeletableCaseTypes")
    void shouldGetCorrectValueForGetDeletableCaseTypes() {
        assertThat(defaultParameterResolver.getDeletableCaseTypes(),
                   is(equalTo(Arrays.asList("Case_Type_01"))));
    }

    @Test
    @DisplayName("should get correct value for getDocumentsDeleteUrl")
    void shouldGetCorrectValueForGetDocumentsHost() {
        assertThat(defaultParameterResolver.getDocumentStoreHost(),
                is(equalTo("http://localhost:4603")));
    }

}
