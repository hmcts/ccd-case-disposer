package uk.gov.hmcts.reform.ccd.parameter;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableList;

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

    @Value("${ccd.document.store.host}")
    private String documentStoreHost;

    @Value("${ccd.role.assignment.host}")
    private String roleAssignmentHost;

    @Value("${ccd.log.and.audit.host}")
    private String logAndAuditHost;

    @Value("${ccd.tasks.host}")
    private String tasksHost;

    @Value("${log.and.audit.enabled}")
    private Boolean isLogAndAuditEnabled;

    @Value("${ccd.role.assignment.check-case-roles-exist}")
    private Boolean checkCaseRolesExist;

    @Value("${app.insights.log.size}")
    private Integer appInsightsLogSize;

    @Value("${thread.max_pool_size}")
    private Integer threadMaxPoolSize;

    @Value("${thread.core_pool_size}")
    private Integer threadCorePoolSize;

    @Value("${thread.queue_capacity}")
    private Integer threadQueueCapacity;

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
                .collect(toUnmodifiableList());
    }

    @Override
    public List<String> getDeletableCaseTypesSimulation() {
        return deletableCaseTypeSimulation.stream()
                .map(quotedItem -> quotedItem.replace("\"", "").strip())
                .collect(toUnmodifiableList());
    }

    @Override
    public List<String> getAllDeletableCaseTypes() {
        return Stream.concat(getDeletableCaseTypes().stream(), getDeletableCaseTypesSimulation().stream())
                .collect(Collectors.toList());
    }

    @Override
    public String getDocumentStoreHost() {
        return documentStoreHost;
    }

    @Override
    public Boolean getCheckCaseRolesExist() {
        return checkCaseRolesExist;
    }

    @Override
    public String getRoleAssignmentsHost() {
        return roleAssignmentHost;
    }

    @Override
    public String getLogAndAuditHost() {
        return logAndAuditHost;
    }

    @Override
    public String getTasksHost() {
        return tasksHost;
    }

    @Override
    public Integer getAppInsightsLogSize() {
        return appInsightsLogSize;
    }

    @Override
    public Boolean isLogAndAuditEnabled() {
        return isLogAndAuditEnabled;
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
}
