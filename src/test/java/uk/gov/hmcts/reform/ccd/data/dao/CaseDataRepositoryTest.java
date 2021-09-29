package uk.gov.hmcts.reform.ccd.data.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.time.LocalDate;
import java.util.List;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

class CaseDataRepositoryTest extends RepositoryTestsFixture {
    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate YESTERDAY = TODAY.minusDays(1L);
    private static final LocalDate TOMORROW = TODAY.plusDays(1L);

    private CaseDataEntity caseDataYesterday;

    @Inject
    private TestEntityManager entityManager;

    @Inject
    private CaseDataRepository underTest;

    @BeforeEach
    void prepare() {
        caseDataYesterday = new CaseDataEntity();
        caseDataYesterday.setId(1L);
        caseDataYesterday.setReference(1L);
        caseDataYesterday.setCaseType("ct-1");
        caseDataYesterday.setResolvedTtl(YESTERDAY);

        CaseDataEntity caseDataToday = new CaseDataEntity();
        caseDataToday.setId(2L);
        caseDataToday.setReference(2L);
        caseDataToday.setCaseType("ct-1");
        caseDataToday.setResolvedTtl(TODAY);

        CaseDataEntity caseDataTomorrow = new CaseDataEntity();
        caseDataTomorrow.setId(3L);
        caseDataTomorrow.setReference(3L);
        caseDataTomorrow.setCaseType("ct-1");
        caseDataTomorrow.setResolvedTtl(TOMORROW);

        List.of(caseDataYesterday, caseDataToday, caseDataTomorrow)
            .forEach(caseData -> entityManager.persist(caseData));
    }

    @Test
    void testFindExpiredCases() {
        final List<CaseDataEntity> expiredCases = underTest.findExpiredCases();

        assertThat(expiredCases)
            .hasSize(1)
            .element(0)
            .satisfies(item -> {
                assertThat(item.getId()).isEqualTo(caseDataYesterday.getId());
                assertThat(item.getReference()).isEqualTo(caseDataYesterday.getReference());
                assertThat(item.getCaseType()).isEqualTo(caseDataYesterday.getCaseType());
                assertThat(item.getResolvedTtl()).isEqualTo(caseDataYesterday.getResolvedTtl());
            });
    }
}
