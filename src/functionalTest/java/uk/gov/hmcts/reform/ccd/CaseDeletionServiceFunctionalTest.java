package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventEntity;
import uk.gov.hmcts.reform.ccd.data.es.CaseDataElasticsearchOperations;

import javax.inject.Inject;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;

//@DataJpaTest
//@RunWith(SpringRunner.class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(classes = TestApplicationConfiguration.class)
@ActiveProfiles("functional")
@Sql("/test_data.sql")
public class CaseDeletionServiceFunctionalTest {

    @Inject
    private CaseDataRepository caseDataRepository;

    @Inject
    private CaseEventRepository caseEventRepository;

    @Inject
    private CaseDataElasticsearchOperations caseDataElasticsearchOperations;

    @Autowired
    private ApplicationExecutor executor;

    private CaseDataEntity caseDataEntity;
    private CaseEventEntity caseEventEntity;

    @Test
    public void testExecuteMethod() {

        Optional<CaseDataEntity> byId = caseDataRepository.findById(1L);

        executor.execute();

        assertNull(caseDataRepository.findById(1L));
        assertNull(caseEventRepository.findById(1L));

    }

}
