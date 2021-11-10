package uk.gov.hmcts.reform.ccd;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import javax.inject.Named;

import static java.util.stream.Collectors.toUnmodifiableList;

@Named
@SuppressWarnings("ALL")
public class ApplicationParameters {

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

    public List<String> getElasticsearchHosts() {
        return elasticsearchHosts.stream()
            .map(quotedHost -> quotedHost.replace("\"", ""))
            .collect(toUnmodifiableList());
    }

    public Integer getElasticsearchRequestTimeout() {
        return elasticsearchRequestTimeout;
    }

    public String getCasesIndexNamePattern() {
        return casesIndexNamePattern;
    }

    public String getCasesIndexType() {
        return casesIndexType;
    }

    public List<String> getDeletableCaseTypes() {
        return deletableCaseTypes.stream()
            .map(quotedHost -> quotedHost.replace("\"", ""))
            .collect(toUnmodifiableList());
    }
}
