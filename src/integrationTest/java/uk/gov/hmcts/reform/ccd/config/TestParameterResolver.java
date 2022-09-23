package uk.gov.hmcts.reform.ccd.config;

import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

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

    @Value("${ccd.document.store.host}")
    private String documentStoreHost;

    @Value("${ccd.role.assignment.host}")
    private String roleAssignmentHost;

    @Value("${ccd.log.and.audit.host}")
    private String logAndAuditHost;

    @Value("${ccd.role.assignment.check-case-roles-exist}")
    private Boolean checkCaseRolesExist;

    @Value("${app.insights.log.size}")
    private Integer appInsightsLogSize;

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
    public String getDocumentStoreHost() {
        return documentStoreHost;
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
    public Boolean getCheckCaseRolesExist() {
        return checkCaseRolesExist;
    }

    @Override
    public Integer getAppInsightsLogSize() {
        return appInsightsLogSize;
    }
}
