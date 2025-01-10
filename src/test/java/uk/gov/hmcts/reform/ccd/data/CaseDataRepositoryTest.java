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
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.JURISDICTION;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.TODAY;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.YESTERDAY;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.liquibase.enabled=false",
    "spring.flyway.enabled=true"
})
@ImportAutoConfiguration({FeignAutoConfiguration.class})
public class CaseDataRepositoryTest {

    @Autowired
    private CaseDataRepository caseDataRepository;

    @BeforeEach
    public void setUp() {
        caseDataRepository.deleteAll();
        caseDataRepository.save(getCaseDataEntity(1L));
        caseDataRepository.save(getCaseDataEntity(2L));
        caseDataRepository.save(getUnexpiredCaseDataEntity(3L));
        caseDataRepository.save(getUnexpiredCaseDataEntity(4L));
    }

    @Test
    void testFindExpiredCases() {
        List<CaseDataEntity> caseDataAll = caseDataRepository.findAll();
        assertThat(caseDataAll).hasSize(4);
        List<CaseDataEntity> caseData = caseDataRepository.findExpiredCases(List.of(DELETABLE_CASE_TYPE));
        assertThat(caseData).hasSize(2);
    }

    @Test
    void testFindByCaseReference() {
        Optional<CaseDataEntity> caseData = caseDataRepository.findByReference(1L);
        assertThat(caseData).isPresent();
        assertThat(caseData.get().getReference()).isEqualTo(1L);
    }

    @Test
    void testDeleteCaseData() {
        Optional<CaseDataEntity> caseData = caseDataRepository.findByReference(1L);
        assertThat(caseData).isPresent();
        caseDataRepository.delete(caseData.get());
        caseData = caseDataRepository.findByReference(1L);
        assertThat(caseData).isNotPresent();
        List<CaseDataEntity> caseDataEntities = caseDataRepository.findAll();
        assertThat(caseDataEntities).hasSize(3);
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

    private CaseDataEntity getUnexpiredCaseDataEntity(final Long id) {
        final CaseDataEntity caseDataEntity = new CaseDataEntity();
        caseDataEntity.setId(id);
        caseDataEntity.setReference(id);
        caseDataEntity.setCaseType(DELETABLE_CASE_TYPE);
        caseDataEntity.setJurisdiction(JURISDICTION);
        caseDataEntity.setResolvedTtl(TODAY);
        return caseDataEntity;
    }
}
