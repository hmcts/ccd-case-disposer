package uk.gov.hmcts.reform.ccd;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import javax.inject.Named;

import static java.util.stream.Collectors.toUnmodifiableList;

@Named
public class ApplicationParameters {

    @Value("#{'${elasticsearch.hosts}'.split(',')}")
    private List<String> elasticsearchHosts;

    @Value("${elasticsearch.request.timeout}")
    private Integer elasticsearchRequestTimeout;

    @Value("${elasticsearch.cases.index.name.pattern}")
    private String casesIndexNamePattern;

    @Value("#{'${expirable.case.types}'.split(',')}")
    private List<String> expirableCaseTypes;

    public String[] getElasticsearchHosts() {
        final List<String> connectionStrings = elasticsearchHosts.stream()
            .map(quotedHost -> quotedHost.replace("\"", ""))
            .collect(toUnmodifiableList());

        return connectionStrings.toArray(String[]::new);
    }

    public Integer getElasticsearchRequestTimeout() {
        return elasticsearchRequestTimeout;
    }

    public String getCasesIndexNamePattern() {
        return casesIndexNamePattern;
    }

    public List<String> getExpirableCaseTypes() {
        return expirableCaseTypes;
    }
}
