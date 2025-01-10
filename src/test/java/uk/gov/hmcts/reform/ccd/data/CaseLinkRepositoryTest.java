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
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.liquibase.enabled=false",
    "spring.flyway.enabled=true"
})
@ImportAutoConfiguration({FeignAutoConfiguration.class})
public class CaseLinkRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CaseLinkRepository caseLinkRepository;

    @BeforeEach
    public void setUp() throws SQLException {
        insertDataIntoDatabase("testData/case_link.sql");
    }

    @Test
    void testFindByCaseId() {
        List<CaseLinkEntity> caseLinkEntities = caseLinkRepository.findByCaseId(7L);
        assertThat(caseLinkEntities).hasSize(1);
    }

    @Test
    void testFindByLinkedCaseId() {
        List<CaseLinkEntity> caseLinkEntities = caseLinkRepository.findByLinkedCaseId(8L);
        assertThat(caseLinkEntities).hasSize(1);
    }

    @Test
    void testFindByCaseIdOrLinkedCaseId() {
        List<CaseLinkEntity> caseLinkEntities = caseLinkRepository.findByCaseIdOrLinkedCaseId(7L);
        assertThat(caseLinkEntities).hasSize(1);
        caseLinkEntities = caseLinkRepository.findByCaseIdOrLinkedCaseId(10L);
        assertThat(caseLinkEntities).hasSize(1);
    }

    @Test
    void testDeleteCaseLink() {
        List<CaseLinkEntity> caseLinkEntities = caseLinkRepository.findByCaseIdOrLinkedCaseId(7L);
        assertThat(caseLinkEntities).hasSize(1);
        caseLinkRepository.delete(caseLinkEntities.getFirst());
        caseLinkEntities = caseLinkRepository.findByCaseIdOrLinkedCaseId(7L);
        assertThat(caseLinkEntities).hasSize(0);
    }
}
