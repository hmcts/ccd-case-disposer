package uk.gov.hmcts.reform.ccd.data.dao;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventEntity;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@Sql("/sql/data/case-events.sql")
class CaseEventRepositoryIntegrationTest extends TestRepositoryFixture {

    @Inject
    private CaseEventRepository underTest;

    @Test
    void testDeleteByCaseDataId() {
        underTest.deleteByCaseDataId(1L);

        final Iterable<CaseEventEntity> entities = underTest.findAll();
        assertThat(entities)
            .hasSize(1)
            .element(0)
            .satisfies(item -> assertThat(item.getCaseDataId()).isEqualTo(2));
    }

}
