package uk.gov.hmcts.reform.ccd.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@Configuration
@ComponentScan(
    basePackages = {"uk.gov.hmcts.reform.ccd"},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ApplicationRunner.class)
)
@EnableJpaRepositories(basePackages = {"uk.gov.hmcts.reform.ccd.data"})
@EntityScan(basePackages = {"uk.gov.hmcts.reform.ccd.data"})
@EnableAutoConfiguration
public class TestApplicationConfiguration {

    @Value("#{'${test.case.types}'.split(',')}")
    private List<String> testCaseTypes;

    @Bean
    public List<String> provideTestCaseTypes() {
        return testCaseTypes.stream()
            .map(caseType -> caseType.replace("\"", ""))
            .collect(toUnmodifiableList());
    }

}
