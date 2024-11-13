package uk.gov.hmcts.reform.ccd.parameter;

import org.springframework.beans.factory.annotation.Value;

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

    @Value("${ccd.case-definition.host}")
    private String caseDefinitionHost;

    @Value("${idam.api.username}")
    private String idamApiUsername;

    @Value("${idam.api.password}")
    private String idamApiPassword;

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

    @Override
    public List<String> getElasticsearchHosts() {
        return elasticsearchHosts.stream()
                .map(quotedHost -> quotedHost.replace("\"", "").strip())
                .collect(toUnmodifiableList());
    }

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

    public String getCaseDefinitionHost() {
        return caseDefinitionHost;
    }

    @Override
    public String getIdamUsername() {
        return idamApiUsername;
    }

    @Override
    public String getIdamPassword() {
        return idamApiPassword;
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
        final String[] result = Optional.ofNullable(System.getProperty(DELETABLE_CASE_TYPES_PROPERTY_SIMULATION))
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
