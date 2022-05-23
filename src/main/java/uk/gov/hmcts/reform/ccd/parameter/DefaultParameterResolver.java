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

    @Value("#{'${deletable.case.types}'.split(',')}")
    private List<String> deletableCaseTypes;

    @Value("#{'${simulated.case.types}'.split(',')}")
    private List<String> deletableCaseTypeSimulation;

    @Value("${dm.remote.documents-delete-url}")
    private String dmRemoteDocumentsDeleteUrl;

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
    public String getDocumentsDeleteUrl() {
        return dmRemoteDocumentsDeleteUrl;
    }
}
