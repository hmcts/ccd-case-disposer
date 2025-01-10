package uk.gov.hmcts.reform.ccd.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.liquibase.enabled=false",
    "spring.flyway.enabled=true"
})
@ImportAutoConfiguration({FeignAutoConfiguration.class})
public class CaseEventRepositoryTest {

    @Autowired
    private CaseEventRepository caseEventRepository;

    @BeforeEach
    public void setUp() {
        caseEventRepository.deleteAll();
        CaseEventEntity caseEventEntity = new CaseEventEntity();
        caseEventEntity.setId(1L);
        caseEventEntity.setCaseDataId(7L);
        caseEventRepository.save(caseEventEntity);
    }

    @Test
    void testFindById() {
        Optional<CaseEventEntity> caseEvent = caseEventRepository.findById(1L);
        assertThat(caseEvent).isPresent();
        assertThat(caseEvent.get().getCaseDataId()).isEqualTo(7L);
    }

    @Test
    void testDeleteByCaseDataId() {
        caseEventRepository.deleteByCaseDataId(7L);
        Optional<CaseEventEntity> caseEvent = caseEventRepository.findById(1L);
        assertThat(caseEvent).isEmpty();
    }


}
