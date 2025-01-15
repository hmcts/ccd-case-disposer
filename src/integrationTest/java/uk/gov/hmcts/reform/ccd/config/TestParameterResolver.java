package uk.gov.hmcts.reform.ccd.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableList;

@SuppressWarnings("ALL")
public class TestParameterResolver implements ParameterResolver {
    public static final String DELETABLE_CASE_TYPES_PROPERTY = "deletable.case.types";
    public static final String DELETABLE_CASE_TYPES_PROPERTY_SIMULATION = "simulated.case.types";

    @Value("#{'${elasticsearch.hosts}'.split(',')}")
    private List<String> elasticsearchHosts;

    @Value("${elasticsearch.request.timeout}")
    private Integer elasticsearchRequestTimeout;

    @Value("${elasticsearch.cases.index.name.pattern}")
    private String casesIndexNamePattern;

    @Value("${elasticsearch.global.search.index.name}")
    private String globalSearchIndexName;

    @Value("${elasticsearch.cases.index.type}")
    private String casesIndexType;

    @Value("${remote.hearing.case.type}")
    private String hearingCaseType;

    @Value("${app.insights.log.size}")
    private Integer appInsightsLogSize;

    @Value("${thread.max_pool_size}")
    private Integer threadMaxPoolSize;

    @Value("${thread.core_pool_size}")
    private Integer threadCorePoolSize;

    @Value("${thread.queue_capacity}")
    private Integer threadQueueCapacity;

    @Value("${request.limit}")
    private Integer requestLimit;

    @Getter
    @Value("${cut-off-time:06:00}")
    private LocalTime cutOffTime;

    @Override
    public Integer getElasticsearchRequestTimeout() {
        return elasticsearchRequestTimeout;
    }

    @Override
    public String getCasesIndexNamePattern() {
        return casesIndexNamePattern;
    }

    @Override
    public String getGlobalSearchIndexName() {
        return globalSearchIndexName;
    }

    @Override
    public String getCasesIndexType() {
        return casesIndexType;
    }

    @Override
    public List<String> getElasticsearchHosts() {
        return elasticsearchHosts.stream()
                .map(quotedHost -> quotedHost.replace("\"", "").strip())
                .collect(toUnmodifiableList());
    }

    @Override
    public List<String> getDeletableCaseTypes() {
        final String[] result = Optional.ofNullable(System.getProperty(DELETABLE_CASE_TYPES_PROPERTY))
                .map(value -> value.strip().split(","))
                .orElse(new String[0]);

        return Arrays.stream(result)
                .map(quotedItem -> quotedItem.replace("\"", "").strip())
                .collect(toUnmodifiableList());
    }

    @Override
    public List<String> getDeletableCaseTypesSimulation() {
        String property = System.getProperty(DELETABLE_CASE_TYPES_PROPERTY_SIMULATION);
        final String[] result = Optional.ofNullable(property)
                .map(value -> value.strip().split(","))
                .orElse(new String[0]);

        return Arrays.stream(result)
                .map(quotedItem -> quotedItem.replace("\"", "").strip())
                .collect(toUnmodifiableList());
    }

    @Override
    public List<String> getAllDeletableCaseTypes() {
        return Stream.concat(getDeletableCaseTypes().stream(), getDeletableCaseTypesSimulation().stream())
                .collect(toUnmodifiableList());
    }

    @Override
    public String getCaseDefinitionHost() {
        return null;
    }

    @Override
    public String getIdamUsername() {
        return null;
    }

    @Override
    public String getIdamPassword() {
        return null;
    }

    @Override
    public String getHearingCaseType() {
        return hearingCaseType;
    }

    @Override
    public Integer getAppInsightsLogSize() {
        return appInsightsLogSize;
    }

    @Override
    public Integer getThreadMaxPoolSize() {
        return threadMaxPoolSize;
    }

    @Override
    public Integer getThreadCorePoolSize() {
        return threadCorePoolSize;
    }

    @Override
    public Integer getThreadQueueCapacity() {
        return threadQueueCapacity;
    }

    @Override
    public Integer getRequestLimit() {
        return requestLimit;
    }

}
