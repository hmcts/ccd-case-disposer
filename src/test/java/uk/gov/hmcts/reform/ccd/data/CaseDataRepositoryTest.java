package uk.gov.hmcts.reform.ccd.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.liquibase.enabled=false",
    "spring.flyway.enabled=true"
})
public class CaseDataRepositoryTest {

    private static final String DELETABLE_CASE_TYPE = "deletable_case_type";
    private static final String JURISDICTION = "deletable_jurisdiction";
    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate YESTERDAY = TODAY.minusDays(1L);

    @Autowired
    private CaseDataRepository caseDataRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() {
        final CaseDataInsertRepository caseDataInsertRepository =
                new CaseDataInsertRepository(entityManager);

        caseDataInsertRepository.saveCaseData(getCaseDataEntity(1L));
        caseDataInsertRepository.saveCaseData(getCaseDataEntity(2L));
    }

    @Test
    void findExpiredCases() {
        final List<CaseDataEntity> caseDataList = caseDataRepository.findAll();
        assertThat(caseDataList).hasSize(2);
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
