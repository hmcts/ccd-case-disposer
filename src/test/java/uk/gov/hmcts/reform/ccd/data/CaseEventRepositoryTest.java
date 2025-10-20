package uk.gov.hmcts.reform.ccd.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventEntity;

import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ImportAutoConfiguration({FeignAutoConfiguration.class})
public class CaseEventRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CaseEventRepository caseEventRepository;

    @BeforeEach
    public void setUp() throws SQLException {
        insertDataIntoDatabase("testData/case_event.sql");
    }

    @Test
    void testFindById() {
        Optional<CaseEventEntity> caseEvent = caseEventRepository.findById(5L);
        assertThat(caseEvent).isPresent();
        assertThat(caseEvent.get().getCaseDataId()).isEqualTo(5L);
    }

    @Test
    void testDeleteByCaseDataId() {
        int deletedEvent = caseEventRepository.deleteByCaseDataId(5L);
        Optional<CaseEventEntity> caseEvent = caseEventRepository.findById(5L);
        assertThat(caseEvent).isEmpty();
        assertThat(deletedEvent).isEqualTo(1);
    }


}
