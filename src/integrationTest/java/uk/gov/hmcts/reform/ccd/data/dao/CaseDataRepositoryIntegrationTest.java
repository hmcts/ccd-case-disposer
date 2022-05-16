package uk.gov.hmcts.reform.ccd.data.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.List;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.CASE_DATA_ENTITIES;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.YESTERDAY;

class CaseDataRepositoryIntegrationTest extends TestRepositoryFixture {
    @Inject
    private TestEntityManager entityManager;

    @Inject
    private ParameterResolver parameterResolver;

    @Inject
    private CaseDataRepository underTest;

    @BeforeEach
    void prepare() {
        CASE_DATA_ENTITIES.forEach(caseData -> entityManager.persist(caseData));
    }

    @Test
    void testFindExpiredCases() {
        final List<CaseDataEntity> expiredCases = underTest.findExpiredCases(parameterResolver.getDeletableCaseTypes());

        assertThat(expiredCases)
            .isNotEmpty()
            .singleElement()
            .satisfies(item -> {
                assertThat(item.getId()).isEqualTo(1L);
                assertThat(item.getReference()).isEqualTo(1L);
                assertThat(item.getCaseType()).isEqualTo(DELETABLE_CASE_TYPE);
                assertThat(item.getResolvedTtl()).isEqualTo(YESTERDAY);
            });

        Mockito.verifyNoInteractions(authTokenGenerator);
    }
}
