package uk.gov.hmcts.reform.ccd.data.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.util.List;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.CASE_DATA_ENTITIES;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.CASE_DATA_YESTERDAY;

class CaseDataRepositoryIntegrationTest extends TestRepositoryFixture {
    @Inject
    private TestEntityManager entityManager;

    @Inject
    private CaseDataRepository underTest;

    @BeforeEach
    void prepare() {
        CASE_DATA_ENTITIES.forEach(caseData -> entityManager.persist(caseData));
    }

    @Test
    void testFindExpiredCases() {
        final List<CaseDataEntity> expiredCases = underTest.findExpiredCases();

        assertThat(expiredCases)
            .hasSize(1)
            .element(0)
            .satisfies(item -> {
                assertThat(item.getId()).isEqualTo(CASE_DATA_YESTERDAY.getId());
                assertThat(item.getReference()).isEqualTo(CASE_DATA_YESTERDAY.getReference());
                assertThat(item.getCaseType()).isEqualTo(CASE_DATA_YESTERDAY.getCaseType());
                assertThat(item.getResolvedTtl()).isEqualTo(CASE_DATA_YESTERDAY.getResolvedTtl());
            });
    }
}
