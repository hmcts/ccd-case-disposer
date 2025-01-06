package uk.gov.hmcts.reform.ccd.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.*;

@DataJpaTest
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.liquibase.enabled=false",
    "spring.flyway.enabled=true",
    "spring.flyway.validateMigrationNaming=true"
})
public class CaseDataRepositoryTest {

    /*@Autowired
    private Flyway flyway;*/

    @Autowired
    private CaseDataRepository caseDataRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() {
        //flyway.clean();
       // flyway.migrate();

        caseDataRepository.deleteAll();
        final CaseDataInsertRepository caseDataInsertRepository =
                new CaseDataInsertRepository(entityManager);

        caseDataInsertRepository.saveCaseData(getCaseDataEntity(1L));
        caseDataInsertRepository.saveCaseData(getCaseDataEntity(2L));
    }

    @Test
    void testFindExpiredCases() {
        List<CaseDataEntity> caseDataAll = caseDataRepository.findAll();
        assertThat(caseDataAll).hasSize(0);
        //CaseDataEntity caseDataEntity = caseDataRepository.findById(1L).orElseThrow();
        List<CaseDataEntity> caseData = caseDataRepository.findExpiredCases(List.of(DELETABLE_CASE_TYPE));
        assertThat(caseData).hasSize(0);
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
