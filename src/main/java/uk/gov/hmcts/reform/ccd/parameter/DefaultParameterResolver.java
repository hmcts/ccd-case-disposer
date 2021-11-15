package uk.gov.hmcts.reform.ccd.parameter;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;

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

    @Value("#{'${deletable.case.types}'.split(',')}")
    private List<String> deletableCaseTypes;

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
    public List<String> getDeletableCaseTypes() {
        return deletableCaseTypes.stream()
            .map(quotedItem -> quotedItem.replace("\"", "").strip())
            .collect(toUnmodifiableList());
    }
}
