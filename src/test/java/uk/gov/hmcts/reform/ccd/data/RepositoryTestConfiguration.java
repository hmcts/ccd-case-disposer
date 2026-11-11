package uk.gov.hmcts.reform.ccd.data;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.flyway.FlywayConfiguration;

@SpringBootConfiguration
@EntityScan(basePackageClasses = CaseDataEntity.class)
@EnableJpaRepositories(basePackageClasses = CaseDataRepository.class)
@Import(FlywayConfiguration.class)
@Profile("test")
class RepositoryTestConfiguration {
}
