package uk.gov.hmcts.reform.ccd.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {

    @Primary
    @Bean(name = "ccd")
    @ConfigurationProperties("datasource.ccd")
    public DataSource ccdDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "evidence")
    @ConfigurationProperties("datasource.evidence")
    public DataSource evidenceDataSource() {
        return DataSourceBuilder.create().build();
    }

}
