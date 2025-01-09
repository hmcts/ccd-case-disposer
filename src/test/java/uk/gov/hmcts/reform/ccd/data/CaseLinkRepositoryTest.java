package uk.gov.hmcts.reform.ccd.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;

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
@ImportAutoConfiguration({FeignAutoConfiguration.class})
public class CaseLinkRepositoryTest {

    @Autowired
    private CaseLinkRepository caseLinkRepository;

    @BeforeEach
    public void setUp() {
        caseLinkRepository.deleteAll();
        caseLinkRepository.save(getCaseLinkEntity(1L,2L));
        caseLinkRepository.save(getCaseLinkEntity(3L,4L));
    }

    @Test
    void testFindByCaseId() {
        List<CaseLinkEntity> caseLinkEntities = caseLinkRepository.findByCaseId(1L);
        assertThat(caseLinkEntities).hasSize(1);
    }

    @Test
    void testFindByLinkedCaseId() {
        List<CaseLinkEntity> caseLinkEntities = caseLinkRepository.findByLinkedCaseId(2L);
        assertThat(caseLinkEntities).hasSize(1);
    }

    @Test
    void testFindByCaseIdOrLinkedCaseId() {
        List<CaseLinkEntity> caseLinkEntities = caseLinkRepository.findByCaseIdOrLinkedCaseId(1L);
        assertThat(caseLinkEntities).hasSize(1);
        caseLinkEntities = caseLinkRepository.findByCaseIdOrLinkedCaseId(4L);
        assertThat(caseLinkEntities).hasSize(1);
    }

    @Test
    void testDeleteCaseLink() {
        List<CaseLinkEntity> caseLinkEntities = caseLinkRepository.findByCaseIdOrLinkedCaseId(1L);
        assertThat(caseLinkEntities).hasSize(1);
        caseLinkRepository.delete(caseLinkEntities.getFirst());
        caseLinkEntities = caseLinkRepository.findByCaseIdOrLinkedCaseId(1L);
        assertThat(caseLinkEntities).hasSize(0);
    }

    private CaseLinkEntity getCaseLinkEntity(final Long caseId, final Long linkedCaseId) {
        final CaseLinkEntity caseLinkEntity = new CaseLinkEntity();
        caseLinkEntity.setCaseId(caseId);
        caseLinkEntity.setLinkedCaseId(linkedCaseId);
        caseLinkEntity.setCaseTypeId(DELETABLE_CASE_TYPE);
        return caseLinkEntity;
    }
}
