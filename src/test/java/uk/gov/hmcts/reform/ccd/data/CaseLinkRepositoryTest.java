package uk.gov.hmcts.reform.ccd.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TaskExecutorConfig.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ImportAutoConfiguration({FeignAutoConfiguration.class})
class CaseLinkRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CaseLinkRepository caseLinkRepository;

    @BeforeEach
    void setUp() throws SQLException {
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
        assertThat(caseLinkEntities).isEmpty();
    }
}
