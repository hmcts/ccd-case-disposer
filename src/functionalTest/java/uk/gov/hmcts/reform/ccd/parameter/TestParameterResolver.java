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

    @Value("${elasticsearch.cases.index.type}")
    private String casesIndexType;

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
}
