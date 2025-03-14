package uk.gov.hmcts.reform.ccd.parameter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DefaultParameterResolverTest {

    private static final String ELASTIC_SEARCH_HOSTS = "elasticsearchHosts";
    private static final String ELASTIC_SEARCH_REQUEST_TIMEOUT = "elasticsearchRequestTimeout";
    private static final String CASE_INDEX_NAME_PATTERN = "casesIndexNamePattern";
    private static final String CASES_INDEX_TYPE = "casesIndexType";

    private static final String GLOBAL_SEARCH_INDEX_NAME = "globalSearchIndexName";

    private static final String IDAM_API_USER_FIELD = "idamApiUsername";
    private static final String IDAM_API_PWD_FIELD = "idamApiPassword";

    private static final String DELETABLE_CASE_TYPES = "deletableCaseTypes";
    private static final String DELETABLE_CASE_TYPE_SIMULATION = "deletableCaseTypeSimulation";

    private static final String THREAD_MAX_POOL_SIZE = "threadMaxPoolSize";

    private static final String THREAD_CORE_POOL_SIZE = "threadCorePoolSize";

    private static final String THREAD_QUEUE_CAPACITY = "threadQueueCapacity";


    protected DefaultParameterResolver defaultParameterResolver = new DefaultParameterResolver();

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

        ReflectionTestUtils.setField(defaultParameterResolver, GLOBAL_SEARCH_INDEX_NAME, "global_index_name");

        ReflectionTestUtils.setField(defaultParameterResolver, IDAM_API_USER_FIELD, "user@email.com");
        ReflectionTestUtils.setField(defaultParameterResolver, IDAM_API_PWD_FIELD, "Pa55w0rd");

        ReflectionTestUtils.setField(defaultParameterResolver,
                                     DELETABLE_CASE_TYPES, Arrays.asList("Case_Type_01"));

        ReflectionTestUtils.setField(defaultParameterResolver,
                                     DELETABLE_CASE_TYPE_SIMULATION, Arrays.asList("Case_Type_02"));

        ReflectionTestUtils.setField(defaultParameterResolver,
                THREAD_MAX_POOL_SIZE, 10);

        ReflectionTestUtils.setField(defaultParameterResolver,
                THREAD_CORE_POOL_SIZE, 5);

        ReflectionTestUtils.setField(defaultParameterResolver,
                THREAD_QUEUE_CAPACITY, 200);

        ReflectionTestUtils.setField(defaultParameterResolver, "cutOffTime", LocalTime.parse("06:00"));
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
    @DisplayName("should get correct value for getGlobalSearchIndexName")
    void shouldGetCorrectValueForGetGlobalSearchIndexName() {
        assertThat(defaultParameterResolver.getGlobalSearchIndexName(),
                   is(equalTo("global_index_name")));
    }

    @Test
    @DisplayName("should get correct value for getIdamUsername")
    void shouldGetCorrectValueForGetIdamUsername() {
        assertThat(defaultParameterResolver.getIdamUsername(),
                   is(equalTo("user@email.com")));
    }

    @Test
    @DisplayName("should get correct value for getIdamPassword")
    void shouldGetCorrectValueForGetIdamPassword() {
        assertThat(defaultParameterResolver.getIdamPassword(),
                   is(equalTo("Pa55w0rd")));
    }

    @Test
    @DisplayName("should get correct value for getDeletableCaseTypes")
    void shouldGetCorrectValueForGetDeletableCaseTypes() {
        assertThat(defaultParameterResolver.getDeletableCaseTypes(),
                   is(equalTo(Arrays.asList("Case_Type_01"))));
    }

    @Test
    @DisplayName("should get correct value for getDeletableCaseTypesSimulation")
    void shouldGetCorrectValueForGetDeletableCaseTypesSimulation() {
        assertThat(defaultParameterResolver.getDeletableCaseTypesSimulation(),
                   is(equalTo(Arrays.asList("Case_Type_02"))));
    }


    @Test
    @DisplayName("should get correct value for threadQueueCapacity")
    void shouldGetCorrectValueForGetThreadQueueCapacity() {
        assertThat(defaultParameterResolver.getThreadQueueCapacity(),
                is(equalTo(200)));
    }

    @Test
    @DisplayName("should get correct value for threadCorePoolSize")
    void shouldGetCorrectValueForGetThreadCorePoolSize() {
        assertThat(defaultParameterResolver.getThreadCorePoolSize(),
                is(equalTo(5)));
    }

    @Test
    @DisplayName("should get correct value for threadMaxPoolSize")
    void shouldGetCorrectValueForGetThreadMaxPoolSize() {
        assertThat(defaultParameterResolver.getThreadMaxPoolSize(),
                is(equalTo(10)));
    }

    @Test
    @DisplayName("should get correct value for getCutOffTime")
    void shouldGetCorrectValueForGetCutOffTime() {
        assertThat(defaultParameterResolver.getCutOffTime(), is(equalTo(LocalTime.parse("06:00"))));
    }

}
