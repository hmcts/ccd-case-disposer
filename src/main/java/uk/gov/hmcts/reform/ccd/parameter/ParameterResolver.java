package uk.gov.hmcts.reform.ccd.parameter;

import java.util.List;

public interface ParameterResolver {
    List<String> getElasticsearchHosts();

    Integer getElasticsearchRequestTimeout();

    String getCasesIndexNamePattern();

    String getCasesIndexType();

    List<String> getDeletableCaseTypes();

    String getDocumentsDeleteUrl();
}
