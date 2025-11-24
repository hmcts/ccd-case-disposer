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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ImportAutoConfiguration({FeignAutoConfiguration.class})
class CaseEventSignificantItemsRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CaseEventSignificantItemsRepository caseEventSignificantItemsRepository;

    @BeforeEach
    void setUp() throws SQLException {
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
        List<CaseEventSignificantItemsEntity> entities =
            caseEventSignificantItemsRepository.findAllById(List.of(12L, 13L, 14L, 15L));
        assertThat(entities).hasSize(4);

        int deletedSF = caseEventSignificantItemsRepository.deleteByCaseDataId(12L);

        List<CaseEventSignificantItemsEntity> deleted =
            caseEventSignificantItemsRepository.findAllById(List.of(12L, 13L, 14L));
        assertThat(deleted).isEmpty();

        Optional<CaseEventSignificantItemsEntity> caseEventSfItem = caseEventSignificantItemsRepository.findById(15L);
        assertThat(caseEventSfItem).isPresent();
        assertThat(caseEventSfItem.get().getId()).isEqualTo(15L);
        assertThat(deletedSF).isEqualTo(3);
    }

}
