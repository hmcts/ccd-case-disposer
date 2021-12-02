package uk.gov.hmcts.reform.ccd.data.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkPrimaryKey;
import uk.gov.hmcts.reform.ccd.fixture.CaseLinkEntityBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.fixture.TestData.DELETABLE_CASE_TYPE;

@Sql("/sql/data/create-cases.sql")
class CaseLinkRepositoryIntegrationTest extends TestRepositoryFixture {
    @Inject
    private CaseLinkRepository underTest;

    private final CaseLinkEntity caseLinkEntity = new CaseLinkEntityBuilder(2L, DELETABLE_CASE_TYPE, 15L)
        .build();
    private final CaseLinkPrimaryKey primaryKey = new CaseLinkPrimaryKey(2L, 15L);

    private final Comparator<CaseLinkEntity> caseLinkEntityComparator = Comparator
        .comparing(CaseLinkEntity::getLinkedCaseId)
        .thenComparing(CaseLinkEntity::getCaseId);

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
        final List<CaseLinkEntity> caseLinkEntities = underTest.findByCaseId(1L);

        assertThat(caseLinkEntities)
            .isNotEmpty()
            .hasSize(3)
            .satisfies(items -> {
                items.sort(caseLinkEntityComparator);
                final CaseLinkEntity caseLinkEntity13 = items.get(0);
                final CaseLinkEntity caseLinkEntity14 = items.get(1);
                final CaseLinkEntity caseLinkEntity24 = items.get(2);

                assertThat(caseLinkEntity13.getCaseId()).isEqualTo(1L);
                assertThat(caseLinkEntity13.getLinkedCaseId()).isEqualTo(13L);
                assertThat(caseLinkEntity13.getCaseTypeId()).isEqualTo("TestAddressBookCaseNoReadFieldAccess");

                assertThat(caseLinkEntity14.getCaseId()).isEqualTo(1L);
                assertThat(caseLinkEntity14.getLinkedCaseId()).isEqualTo(14L);
                assertThat(caseLinkEntity14.getCaseTypeId()).isEqualTo("TestAddressBookCase");

                assertThat(caseLinkEntity24.getCaseId()).isEqualTo(1L);
                assertThat(caseLinkEntity24.getLinkedCaseId()).isEqualTo(24L);
                assertThat(caseLinkEntity24.getCaseTypeId()).isEqualTo("TestAddress");
            });
    }

    @Test
    void testFindByLinkedCaseIdWhenSingleElement() {
        final List<CaseLinkEntity> caseLinkEntities = underTest.findByLinkedCaseId(23L);

        assertThat(caseLinkEntities)
            .isNotEmpty()
            .singleElement()
            .satisfies(item -> {
                assertThat(item.getCaseId()).isEqualTo(2L);
                assertThat(item.getLinkedCaseId()).isEqualTo(23L);
                assertThat(item.getCaseTypeId()).isEqualTo("TestAccess");
            });
    }

    @Test
    void testFindByLinkedCaseIdWhenMultipleElements() {
        final List<CaseLinkEntity> caseLinkEntities = underTest.findByLinkedCaseId(24L);

        assertThat(caseLinkEntities)
            .isNotEmpty()
            .hasSize(2)
            .satisfies(items -> {
                final CaseLinkEntity caseLinkEntity24_1 = items.get(0);
                final CaseLinkEntity caseLinkEntity24_2 = items.get(1);

                assertThat(caseLinkEntity24_1.getCaseId()).isEqualTo(1L);
                assertThat(caseLinkEntity24_1.getLinkedCaseId()).isEqualTo(24L);
                assertThat(caseLinkEntity24_1.getCaseTypeId()).isEqualTo("TestAddress");

                assertThat(caseLinkEntity24_2.getCaseId()).isEqualTo(2L);
                assertThat(caseLinkEntity24_2.getLinkedCaseId()).isEqualTo(24L);
                assertThat(caseLinkEntity24_2.getCaseTypeId()).isEqualTo("TestAddress");
            });
    }

    @Test
    void testFindAllByLinkedCaseId() {
        final List<CaseLinkEntity> caseLinkEntities = underTest.findAllByLinkedCaseId(List.of(13L, 14L, 23L, 24L, 32L));

        assertThat(caseLinkEntities)
            .isNotEmpty()
            .hasSize(6)
            .satisfies(items -> {
                items.sort(caseLinkEntityComparator);
                final CaseLinkEntity caseLinkEntity13 = items.get(0);
                final CaseLinkEntity caseLinkEntity14 = items.get(1);
                final CaseLinkEntity caseLinkEntity23 = items.get(2);
                final CaseLinkEntity caseLinkEntity24_1 = items.get(3);
                final CaseLinkEntity caseLinkEntity24_2 = items.get(4);
                final CaseLinkEntity caseLinkEntity32 = items.get(5);

                assertThat(caseLinkEntity13.getCaseId()).isEqualTo(1L);
                assertThat(caseLinkEntity13.getLinkedCaseId()).isEqualTo(13L);
                assertThat(caseLinkEntity13.getCaseTypeId()).isEqualTo("TestAddressBookCaseNoReadFieldAccess");
                assertThat(caseLinkEntity14.getCaseId()).isEqualTo(1L);
                assertThat(caseLinkEntity14.getLinkedCaseId()).isEqualTo(14L);
                assertThat(caseLinkEntity14.getCaseTypeId()).isEqualTo("TestAddressBookCase");
                assertThat(caseLinkEntity24_1.getCaseId()).isEqualTo(1L);
                assertThat(caseLinkEntity24_1.getLinkedCaseId()).isEqualTo(24L);
                assertThat(caseLinkEntity24_1.getCaseTypeId()).isEqualTo("TestAddress");

                assertThat(caseLinkEntity23.getCaseId()).isEqualTo(2L);
                assertThat(caseLinkEntity23.getLinkedCaseId()).isEqualTo(23L);
                assertThat(caseLinkEntity23.getCaseTypeId()).isEqualTo("TestAccess");
                assertThat(caseLinkEntity24_2.getCaseId()).isEqualTo(2L);
                assertThat(caseLinkEntity24_2.getLinkedCaseId()).isEqualTo(24L);
                assertThat(caseLinkEntity24_2.getCaseTypeId()).isEqualTo("TestAddress");

                assertThat(caseLinkEntity32.getCaseId()).isEqualTo(3L);
                assertThat(caseLinkEntity32.getLinkedCaseId()).isEqualTo(32L);
                assertThat(caseLinkEntity32.getCaseTypeId()).isEqualTo("TestAccess");
            });
    }

}
