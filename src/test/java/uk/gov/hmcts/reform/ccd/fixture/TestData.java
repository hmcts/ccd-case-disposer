package uk.gov.hmcts.reform.ccd.fixture;

import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;

public interface TestData {
    String DELETABLE_CASE_TYPE = "deletable_case_type";
    String NON_DELETABLE_CASE_TYPE = "non_deletable_case_type";
    String DELETABLE_CASE_TYPE_SIMULATION = "deletable_case_type_simulation";
    String INDEX_NAME_PATTERN = "%s_cases";

    LocalDate TODAY = LocalDate.now();
    LocalDate YESTERDAY = TODAY.minusDays(1L);
    CaseDataEntity DELETABLE_CASE_ENTITY_WITH_PAST_TTL = new CaseDataEntityBuilder(1L)
            .withReference(1L)
            .withCaseType(DELETABLE_CASE_TYPE)
            .withResolvedTtl(YESTERDAY)
            .build();
    CaseDataEntity DELETABLE_CASE_ENTITY2_WITH_PAST_TTL = new CaseDataEntityBuilder(1000L)
            .withReference(1000L)
            .withCaseType(DELETABLE_CASE_TYPE)
            .withResolvedTtl(YESTERDAY)
            .build();
    CaseDataEntity LINKED_CASE_ENTITY_10 = new CaseDataEntityBuilder(10L)
            .withReference(10L)
            .withCaseType(DELETABLE_CASE_TYPE)
            .withResolvedTtl(YESTERDAY)
            .build();
    CaseDataEntity LINKED_CASE_ENTITY_11 = new CaseDataEntityBuilder(11L)
            .withReference(11L)
            .withCaseType(DELETABLE_CASE_TYPE)
            .withResolvedTtl(YESTERDAY)
            .build();
    CaseDataEntity LINKED_CASE_ENTITY_100 = new CaseDataEntityBuilder(100L)
            .withReference(100L)
            .withCaseType(NON_DELETABLE_CASE_TYPE)
            .withResolvedTtl(YESTERDAY)
            .build();
    CaseData DELETABLE_CASE_DATA_WITH_PAST_TTL = new CaseData(1L, 1L, DELETABLE_CASE_TYPE, YESTERDAY, 1L, null);
    CaseData DELETABLE_CASE_DATA4_WITH_PAST_TTL = new CaseData(4L, 4L, DELETABLE_CASE_TYPE, YESTERDAY, 4L, null);
    CaseFamily DELETABLE_CASE_FAMILY = new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL,
            asList(DELETABLE_CASE_DATA4_WITH_PAST_TTL,
                    DELETABLE_CASE_DATA_WITH_PAST_TTL,
                    DELETABLE_CASE_DATA_WITH_PAST_TTL));
    CaseData DELETABLE_CASE_DATA5_WITH_PAST_TTL = new CaseData(5L, 5L, DELETABLE_CASE_TYPE, YESTERDAY, 5L, null);
    CaseData DELETABLE_CASE_DATA03_WITH_PAST_TTL = new CaseData(6L, 6L, DELETABLE_CASE_TYPE, YESTERDAY, 6L, null);
    CaseData DELETABLE_CASE_DATA04_WITH_PAST_TTL = new CaseData(7L, 7L, DELETABLE_CASE_TYPE, YESTERDAY, 7L, null);
    CaseData DELETABLE_CASE_DATA05_WITH_PAST_TTL = new CaseData(8L, 8L, DELETABLE_CASE_TYPE, YESTERDAY, 8L, null);
    CaseData DELETABLE_CASE_DATA06_WITH_PAST_TTL = new CaseData(9L, 9L, DELETABLE_CASE_TYPE, YESTERDAY, 9L, null);
    CaseData DELETABLE_CASE_DATA07_WITH_PAST_TTL = new CaseData(91L, 91L, DELETABLE_CASE_TYPE, YESTERDAY, 91L, null);
    CaseData DELETABLE_CASE_DATA08_WITH_PAST_TTL = new CaseData(92L, 92L, DELETABLE_CASE_TYPE, YESTERDAY, 92L, null);
    CaseData DELETABLE_CASE_DATA2_WITH_PAST_TTL =
            new CaseData(1000L, 1000L, DELETABLE_CASE_TYPE, YESTERDAY, 1000L, null);
    CaseData NON_DELETABLE_CASE_DATA_WITH_PAST_TTL =
            new CaseData(21L, 21L, NON_DELETABLE_CASE_TYPE, YESTERDAY, 21L, null);
    CaseFamily NON_DELETABLE_CASE_FAMILY = new CaseFamily(NON_DELETABLE_CASE_DATA_WITH_PAST_TTL,
            asList(NON_DELETABLE_CASE_DATA_WITH_PAST_TTL,
                    NON_DELETABLE_CASE_DATA_WITH_PAST_TTL));
    CaseData DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1 = new CaseData(30L,
            30L,
            DELETABLE_CASE_TYPE_SIMULATION,
            YESTERDAY,
            30L,
            null);
    CaseData DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_2 = new CaseData(31L,
            31L,
            DELETABLE_CASE_TYPE_SIMULATION,
            YESTERDAY,
            31L,
            null);
    CaseFamily MIXTURE_OF_DELETABLE_AND_SIMULATION_CASE_FAMILY = new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL,
            asList(DELETABLE_CASE_DATA4_WITH_PAST_TTL,
                    DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1,
                    DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_2));
    CaseFamily DELETABLE_CASE_FAMILY_SIMULATION = new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1,
            asList(DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1,
                    DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_2));
    CaseData LINKED_CASE_DATA_R10 = new CaseData(10L, 10L, DELETABLE_CASE_TYPE, YESTERDAY, 10L, null);
    CaseData LINKED_CASE_DATA_R11 = new CaseData(11L, 11L, DELETABLE_CASE_TYPE, YESTERDAY, 11L, null);
    CaseData LINKED_CASE_DATA_R12 = new CaseData(12L, 12L, DELETABLE_CASE_TYPE, YESTERDAY, 12L, null);
    CaseData LINKED_CASE_DATA_R13 = new CaseData(13L, 13L, DELETABLE_CASE_TYPE, YESTERDAY, 13L, null);
    CaseData LINKED_CASE_DATA_R100 = new CaseData(100L, 100L, NON_DELETABLE_CASE_TYPE, YESTERDAY, 100L, null);
    LocalDate TOMORROW = TODAY.plusDays(1L);
    CaseDataEntity DELETABLE_CASE_ENTITY_WITH_FUTURE_TTL = new CaseDataEntityBuilder(3L)
            .withReference(3L)
            .withCaseType(DELETABLE_CASE_TYPE)
            .withResolvedTtl(TOMORROW)
            .build();
    CaseData DELETABLE_CASE_DATA_WITH_FUTURE_TTL = new CaseData(3L, 3L, DELETABLE_CASE_TYPE, TOMORROW, 3L, null);
    CaseData LINKED_CASE_DATA_R101 = new CaseData(101L, 101L, DELETABLE_CASE_TYPE, TOMORROW, 101L, null);
    CaseDataEntity DELETABLE_CASE_ENTITY_WITH_TODAY_TTL = new CaseDataEntityBuilder(2L)
            .withReference(2L)
            .withCaseType(DELETABLE_CASE_TYPE)
            .withResolvedTtl(TODAY)
            .build();
    List<CaseDataEntity> CASE_DATA_ENTITIES = List.of(
            DELETABLE_CASE_ENTITY_WITH_PAST_TTL,
            DELETABLE_CASE_ENTITY_WITH_TODAY_TTL,
            DELETABLE_CASE_ENTITY_WITH_FUTURE_TTL
    );
    CaseData DELETABLE_CASE_DATA_WITH_TODAY_TTL = new CaseData(2L, 2L, DELETABLE_CASE_TYPE, TODAY, 2L, null);
}
