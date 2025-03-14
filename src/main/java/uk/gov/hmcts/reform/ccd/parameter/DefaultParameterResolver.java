package uk.gov.hmcts.reform.ccd.parameter;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("ALL")
public class DefaultParameterResolver implements ParameterResolver {

    @Value("#{'${elasticsearch.hosts}'.split(',')}")
    private List<String> elasticsearchHosts;

    @Value("${elasticsearch.request.timeout}")
    private Integer elasticsearchRequestTimeout;

    @Value("${elasticsearch.cases.index.name.pattern}")
    private String casesIndexNamePattern;

    @Value("${elasticsearch.cases.index.type}")
    private String casesIndexType;

    @Value("${elasticsearch.global.search.index.name}")
    private String globalSearchIndexName;

    @Value("${idam.api.username}")
    private String idamApiUsername;

    @Value("${idam.api.password}")
    private String idamApiPassword;

    @Value("#{'${deletable.case.types}'.split(',')}")
    private List<String> deletableCaseTypes;

    @Value("#{'${simulated.case.types}'.split(',')}")
    private List<String> deletableCaseTypeSimulation;

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
    @Value("${cut-off-time}")
    private LocalTime cutOffTime;

    @Override
    public List<String> getElasticsearchHosts() {
        return elasticsearchHosts.stream()
                .map(quotedHost -> quotedHost.replace("\"", "").strip())
                .toList();
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
    public String getCasesIndexType() {
        return casesIndexType;
    }

    @Override
    public String getGlobalSearchIndexName() {
        return globalSearchIndexName;
    }

    @Override
    public String getCaseDefinitionHost() {
        return null;
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
        return deletableCaseTypes.stream()
                .map(quotedItem -> quotedItem.replace("\"", "").strip())
                .toList();
    }

    @Override
    public List<String> getDeletableCaseTypesSimulation() {
        return deletableCaseTypeSimulation.stream()
                .map(quotedItem -> quotedItem.replace("\"", "").strip())
                .toList();
    }

    @Override
    public List<String> getAllDeletableCaseTypes() {
        return Stream.concat(getDeletableCaseTypes().stream(), getDeletableCaseTypesSimulation().stream())
                .toList();
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
