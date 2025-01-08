package uk.gov.hmcts.reform.ccd.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.config.TestConfig;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.JURISDICTION;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.YESTERDAY;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaRepositories(basePackages = {"uk.gov.hmcts.reform.ccd.data"})
@EntityScan(basePackages = {"uk.gov.hmcts.reform.ccd.data.entity"})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.liquibase.enabled=false",
    "spring.flyway.enabled=true"
})
@ContextConfiguration(classes = {TestConfig.class})
public class CaseDataRepositoryTest {

    @Autowired
    private CaseDataRepository caseDataRepository;

    @BeforeEach
    public void setUp() {
        caseDataRepository.deleteAll();
        CaseDataEntity caseDataEntity = new CaseDataEntity();
        caseDataRepository.save(getCaseDataEntity(1L));
        caseDataRepository.save(getCaseDataEntity(2L));
    }

    @Test
    void testFindExpiredCases() {
        System.out.println("testing");
        List<CaseDataEntity> caseDataAll = caseDataRepository.findAll();
        assertThat(caseDataAll).hasSize(2);
        List<CaseDataEntity> caseData = caseDataRepository.findExpiredCases(List.of(DELETABLE_CASE_TYPE));
        assertThat(caseData).hasSize(2);
    }


    private CaseDataEntity getCaseDataEntity(final Long id) {
        final CaseDataEntity caseDataEntity = new CaseDataEntity();
        caseDataEntity.setId(id);
        caseDataEntity.setReference(id);
        caseDataEntity.setCaseType(DELETABLE_CASE_TYPE);
        caseDataEntity.setJurisdiction(JURISDICTION);
        caseDataEntity.setResolvedTtl(YESTERDAY);
        return caseDataEntity;
    }
}
