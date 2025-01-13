package uk.gov.hmcts.reform.ccd.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ImportAutoConfiguration({FeignAutoConfiguration.class})
public class CaseDataRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CaseDataRepository caseDataRepository;

    @BeforeEach
    public void setUp() throws SQLException {
        insertDataIntoDatabase("testData/case_data.sql");
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
        assertThat(caseData).isEmpty();
        List<CaseDataEntity> caseDataEntities = caseDataRepository.findAll();
        assertThat(caseDataEntities).hasSize(3);
    }

}
