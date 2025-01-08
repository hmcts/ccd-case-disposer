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
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
public class CaseEventRepositoryTest {

    @Autowired
    private CaseEventRepository caseEventRepository;

    @BeforeEach
    public void setUp() {
        caseEventRepository.deleteAll();
        CaseEventEntity caseEventEntity = new CaseEventEntity();
        caseEventEntity.setId(1L);
        caseEventEntity.setCaseDataId(1L);
        caseEventRepository.save(caseEventEntity);
    }

    @Test
    void testDeleteByCaseDataId() {
        caseEventRepository.deleteByCaseDataId(1L);
        Optional<CaseEventEntity> caseEvent = caseEventRepository.findById(1L);
        assertThat(caseEvent).isEmpty();
    }

    @Test
    void testFindById() {
        Optional<CaseEventEntity> caseEvent = caseEventRepository.findById(1L);
        assertThat(caseEvent).isPresent();
        assertThat(caseEvent.get().getCaseDataId()).isEqualTo(1L);
    }
}
