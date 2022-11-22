package uk.gov.hmcts.reform.ccd.parameter;

import java.util.List;

public interface ParameterResolver {
    List<String> getElasticsearchHosts();

    Integer getElasticsearchRequestTimeout();

    String getCasesIndexNamePattern();

    String getGlobalSearchIndexName();

    String getCasesIndexType();

    List<String> getDeletableCaseTypes();

    List<String> getDeletableCaseTypesSimulation();

    List<String> getAllDeletableCaseTypes();

    String getCaseDefinitionHost();

    String getIdamUsername();

    String getIdamPassword();

    String getDocumentStoreHost();

    String getRoleAssignmentsHost();

    String getLogAndAuditHost();

    Boolean getCheckCaseRolesExist();

    Integer getAppInsightsLogSize();

    Boolean isLogAndAuditEnabled();

    Integer getThreadMaxPoolSize();

    Integer getThreadCorePoolSize();

    Integer getThreadQueueCapacity();
}
