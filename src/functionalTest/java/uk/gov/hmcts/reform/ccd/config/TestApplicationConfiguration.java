package uk.gov.hmcts.reform.ccd.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.parameter.TestParameterResolver;

import javax.sql.DataSource;

@Configuration
@ComponentScan(
    basePackages = {"uk.gov.hmcts.reform.ccd"},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ApplicationRunner.class)
)
@EnableJpaRepositories(basePackages = {"uk.gov.hmcts.reform.ccd.data"})
@EntityScan(basePackages = {"uk.gov.hmcts.reform.ccd.data"})
@EnableAutoConfiguration
public class TestApplicationConfiguration {

    @Bean
    public DataSource provideDataSource() {
        return DataSourceBuilder.create()
            .build();
    }

    @Bean
    public ParameterResolver provideTestParameterResolver() {
        return new TestParameterResolver();
    }

}
