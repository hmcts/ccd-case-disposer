package uk.gov.hmcts.reform.ccd.data.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkPrimaryKey;
import uk.gov.hmcts.reform.ccd.fixture.CaseLinkEntityBuilder;

import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.CASE_TYPE;

@Sql("/sql/data/create-cases.sql")
class CaseLinkRepositoryIntegrationTest extends TestRepositoryFixture {
    @Inject
    private CaseLinkRepository underTest;

    private final CaseLinkEntity caseLinkEntity = new CaseLinkEntityBuilder(2L, CASE_TYPE, 15L)
        .build();
    private final CaseLinkPrimaryKey primaryKey = new CaseLinkPrimaryKey(2L, CASE_TYPE);

    @BeforeEach
    void prepare() {
        underTest.save(caseLinkEntity);
    }

    @Test
    void testFindCaseLinkEntityById() {
        final Optional<CaseLinkEntity> optionalEntity = underTest.findById(primaryKey);

        assertThat(optionalEntity).isPresent();
    }

    @Test
    void testDeleteCaseLinkEntity() {
        underTest.deleteById(primaryKey);

        final Optional<CaseLinkEntity> optionalEntity = underTest.findById(primaryKey);
        assertThat(optionalEntity).isNotPresent();
    }

    @Test
    void testFindByCaseId() {
        final List<CaseLinkEntity> caseLinks = List.of(
            new CaseLinkEntityBuilder(1L, "TestAddressBookCaseNoReadFieldAccess", 13L).build(),
            new CaseLinkEntityBuilder(1L, "TestAddressBookCase", 14L).build()
        );

        final List<CaseLinkEntity> caseLinkEntities = underTest.findByCaseId(1L);

        assertThat(caseLinkEntities)
            .isNotEmpty()
            .hasSameElementsAs(caseLinks);
    }

}
