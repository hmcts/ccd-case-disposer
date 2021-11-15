package uk.gov.hmcts.reform.ccd.data.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import uk.gov.hmcts.reform.ccd.ApplicationParameters;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.util.List;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.CASE_DATA_ENTITIES;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_WITH_PAST_TTL;

class CaseDataRepositoryIntegrationTest extends TestRepositoryFixture {
    @Inject
    private TestEntityManager entityManager;

    @Inject
    private ApplicationParameters parameters;

    @Inject
    private CaseDataRepository underTest;

    @BeforeEach
    void prepare() {
        CASE_DATA_ENTITIES.forEach(caseData -> entityManager.persist(caseData));
    }

    @Test
    void testFindExpiredCases() {
        final List<CaseDataEntity> expiredCases = underTest.findExpiredCases(parameters.getDeletableCaseTypes());

        assertThat(expiredCases)
            .hasSize(1)
            .element(0)
            .satisfies(item -> {
                assertThat(item.getId()).isEqualTo(DELETABLE_CASE_WITH_PAST_TTL.getId());
                assertThat(item.getReference()).isEqualTo(DELETABLE_CASE_WITH_PAST_TTL.getReference());
                assertThat(item.getCaseType()).isEqualTo(DELETABLE_CASE_WITH_PAST_TTL.getCaseType());
                assertThat(item.getResolvedTtl()).isEqualTo(DELETABLE_CASE_WITH_PAST_TTL.getResolvedTtl());
            });
    }
}
