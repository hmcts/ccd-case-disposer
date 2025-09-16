package uk.gov.hmcts.reform.ccd.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventSignificantItemsEntity;

import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ImportAutoConfiguration({FeignAutoConfiguration.class})
public class CaseEventSignificantItemsRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CaseEventSignificantItemsRepository caseEventSignificantItemsRepository;

    @BeforeEach
    public void setUp() throws SQLException {
        insertDataIntoDatabase("testData/case_event_significant_items.sql");
    }

    @Test
    void testFindByCaseEventId() {
        Optional<CaseEventSignificantItemsEntity> caseEventSignificantItemsEntity =
            caseEventSignificantItemsRepository.findById(12L);
        assertThat(caseEventSignificantItemsEntity).isPresent();
        assertThat(caseEventSignificantItemsEntity.get().getCaseEventId()).isEqualTo(12L);
    }

    @Test
    void testDeleteCaseEventSignificantItem() {
        Optional<CaseEventSignificantItemsEntity> caseEventSignificantItemsEntity =
            caseEventSignificantItemsRepository.findById(12L);
        caseEventSignificantItemsRepository.delete(caseEventSignificantItemsEntity.get());

        Optional<CaseEventSignificantItemsEntity> caseEventSignificantItemsEntity1 =
            caseEventSignificantItemsRepository.findById(13L);
        caseEventSignificantItemsRepository.delete(caseEventSignificantItemsEntity1.get());

        Optional<CaseEventSignificantItemsEntity> caseEventSignificantItemsEntity2 =
            caseEventSignificantItemsRepository.findById(14L);
        caseEventSignificantItemsRepository.delete(caseEventSignificantItemsEntity2.get());

        caseEventSignificantItemsEntity = caseEventSignificantItemsRepository.findById(12L);
        assertThat(caseEventSignificantItemsEntity).isEmpty();

        caseEventSignificantItemsEntity1 = caseEventSignificantItemsRepository.findById(13L);
        assertThat(caseEventSignificantItemsEntity1).isEmpty();

        caseEventSignificantItemsEntity2 = caseEventSignificantItemsRepository.findById(14L);
        assertThat(caseEventSignificantItemsEntity2).isEmpty();
    }

}
