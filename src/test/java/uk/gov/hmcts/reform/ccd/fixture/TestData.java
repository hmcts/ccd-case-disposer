package uk.gov.hmcts.reform.ccd.fixture;

import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;

public interface TestData {
    String DELETABLE_CASE_TYPE = "deletable_case_type";
    String JURISDICTION = "deletable_jurisdiction";
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
    CaseData DELETABLE_CASE_DATA_WITH_PAST_TTL = new CaseData(1L, 1L, DELETABLE_CASE_TYPE,
            JURISDICTION,
            YESTERDAY, 1L, null);
    CaseData DELETABLE_CASE_DATA4_WITH_PAST_TTL = new CaseData(4L, 4L, DELETABLE_CASE_TYPE,
            JURISDICTION,
            YESTERDAY, 4L, null);
    CaseData DELETABLE_CASE_DATA27_WITH_PAST_TTL = new CaseData(27L, 27L, DELETABLE_CASE_TYPE,
                                                               JURISDICTION,
                                                               YESTERDAY, 27L, null);
    CaseFamily DELETABLE_CASE_FAMILY = new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL,
            asList(DELETABLE_CASE_DATA4_WITH_PAST_TTL,
                    DELETABLE_CASE_DATA_WITH_PAST_TTL,
                    DELETABLE_CASE_DATA_WITH_PAST_TTL));
    CaseData DELETABLE_CASE_DATA5_WITH_PAST_TTL = new CaseData(5L, 5L, DELETABLE_CASE_TYPE,
            JURISDICTION, YESTERDAY, 5L, null);
    CaseData DELETABLE_CASE_DATA03_WITH_PAST_TTL = new CaseData(6L, 6L, DELETABLE_CASE_TYPE, JURISDICTION,
            YESTERDAY, 6L, null);
    CaseData DELETABLE_CASE_DATA04_WITH_PAST_TTL = new CaseData(7L, 7L, DELETABLE_CASE_TYPE,
            JURISDICTION, YESTERDAY, 7L, null);
    CaseData DELETABLE_CASE_DATA05_WITH_PAST_TTL = new CaseData(8L, 8L, DELETABLE_CASE_TYPE, JURISDICTION,
            YESTERDAY, 8L, null);
    CaseData DELETABLE_CASE_DATA06_WITH_PAST_TTL = new CaseData(9L, 9L, DELETABLE_CASE_TYPE, JURISDICTION,
            YESTERDAY, 9L, null);
    CaseData DELETABLE_CASE_DATA07_WITH_PAST_TTL = new CaseData(91L, 91L, DELETABLE_CASE_TYPE,
            JURISDICTION, YESTERDAY, 91L, null);
    CaseData DELETABLE_CASE_DATA08_WITH_PAST_TTL = new CaseData(92L, 92L, DELETABLE_CASE_TYPE, JURISDICTION,
            YESTERDAY, 92L, null);

    CaseData DELETABLE_CASE_DATA09_WITH_PAST_TTL = new CaseData(10L, 10L, DELETABLE_CASE_TYPE,
                                                                JURISDICTION, YESTERDAY, 1L, null);
    CaseData DELETABLE_CASE_DATA10_WITH_PAST_TTL = new CaseData(11L, 11L, DELETABLE_CASE_TYPE,
                                                                JURISDICTION, YESTERDAY, 1L, null);
    CaseData DELETABLE_CASE_DATA11_WITH_PAST_TTL = new CaseData(12L, 12L, DELETABLE_CASE_TYPE,
                                                                JURISDICTION, YESTERDAY, 4L, null);

    CaseData DELETABLE_CASE_DATA2_WITH_PAST_TTL =
            new CaseData(1000L, 1000L, DELETABLE_CASE_TYPE, JURISDICTION,
                    YESTERDAY, 1000L, null);
    CaseData NON_DELETABLE_CASE_DATA_WITH_PAST_TTL =
            new CaseData(21L, 21L, NON_DELETABLE_CASE_TYPE, JURISDICTION,
                    YESTERDAY, 21L, null);
    CaseData NON_DELETABLE_CASE_DATA_WITH_MISSING_TTL =
        new CaseData(22L, 22L, NON_DELETABLE_CASE_TYPE, JURISDICTION, null, 22L, null);
    CaseFamily NON_DELETABLE_CASE_FAMILY = new CaseFamily(NON_DELETABLE_CASE_DATA_WITH_PAST_TTL,
            asList(NON_DELETABLE_CASE_DATA_WITH_PAST_TTL,
                    NON_DELETABLE_CASE_DATA_WITH_PAST_TTL));
    CaseData DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1 = new CaseData(30L,
            30L,
            DELETABLE_CASE_TYPE_SIMULATION,
            JURISDICTION,
            YESTERDAY,
            30L,
            null);
    CaseData DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_2 = new CaseData(31L,
            31L,
            DELETABLE_CASE_TYPE_SIMULATION,
            JURISDICTION,
            YESTERDAY,
            31L,
            null);
    CaseFamily DELETABLE_CASE_FAMILY_SIMULATION = new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1,
            asList(DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1,
                    DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_2));

    CaseFamily FAILED_CASE_FAMILY = new CaseFamily(DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1,
            asList(DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1,
                    DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_2));
    CaseData LINKED_CASE_DATA_R10 = new CaseData(10L, 10L, DELETABLE_CASE_TYPE, JURISDICTION,
            YESTERDAY, 10L, null);
    CaseData LINKED_CASE_DATA_R11 = new CaseData(11L, 11L, DELETABLE_CASE_TYPE, JURISDICTION, YESTERDAY,
            11L,
             null);
    CaseData LINKED_CASE_DATA_R12 = new CaseData(12L, 12L, DELETABLE_CASE_TYPE, JURISDICTION, YESTERDAY,
            12L,
            null);
    CaseData LINKED_CASE_DATA_R13 = new CaseData(13L, 13L, DELETABLE_CASE_TYPE, JURISDICTION,YESTERDAY,
            13L,
            null);
    CaseData LINKED_CASE_DATA_R100 = new CaseData(100L, 100L, NON_DELETABLE_CASE_TYPE, JURISDICTION,
            YESTERDAY,
            100L,
            null);
    LocalDate TOMORROW = TODAY.plusDays(1L);
    CaseDataEntity DELETABLE_CASE_ENTITY_WITH_FUTURE_TTL = new CaseDataEntityBuilder(3L)
            .withReference(3L)
            .withCaseType(DELETABLE_CASE_TYPE)
            .withResolvedTtl(TOMORROW)
            .build();
    CaseData DELETABLE_CASE_DATA_WITH_FUTURE_TTL = new CaseData(3L, 3L, DELETABLE_CASE_TYPE, JURISDICTION,
            TOMORROW,
            3L,
            null);
    CaseData LINKED_CASE_DATA_R101 = new CaseData(101L, 101L, DELETABLE_CASE_TYPE, JURISDICTION,TOMORROW,
            101L,
            null);
    CaseData LINKED_CASE_DATA_MISSING_TTL_R102 = new CaseData(102L, 102L, DELETABLE_CASE_TYPE, JURISDICTION,
            null,
            101L,
            null);
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
    CaseData DELETABLE_CASE_DATA_WITH_TODAY_TTL = new CaseData(2L, 2L, DELETABLE_CASE_TYPE, JURISDICTION,
            TODAY,
            2L,
            null);

    CaseData DELETABLE_CASE_DATA80_WITH_PAST_TTL = new CaseData(80L, 80L, DELETABLE_CASE_TYPE,
                                                                JURISDICTION, YESTERDAY, 1L, null);

    CaseData DELETABLE_CASE_DATA81_WITH_PAST_TTL = new CaseData(81L, 81L, DELETABLE_CASE_TYPE,
                                                                JURISDICTION, YESTERDAY, 1L, null);

    CaseData DELETABLE_CASE_DATA81_DUPLICATE_WITH_PAST_TTL = new CaseData(81L, 81L, DELETABLE_CASE_TYPE,
                                                                JURISDICTION, YESTERDAY, 4L, null);

    CaseData DELETABLE_CASE_DATA82_WITH_PAST_TTL = new CaseData(82L, 82L, DELETABLE_CASE_TYPE,
                                                                JURISDICTION, YESTERDAY, 4L, null);

    CaseData DELETABLE_CASE_DATA83_WITH_PAST_TTL = new CaseData(83L, 83L, DELETABLE_CASE_TYPE,
                                                                JURISDICTION, YESTERDAY, 6L, null);

    List<CaseData>  linkedCases = List.of(DELETABLE_CASE_DATA80_WITH_PAST_TTL,
                                          DELETABLE_CASE_DATA81_WITH_PAST_TTL);

    List<CaseData>  linkedCases2 = List.of(DELETABLE_CASE_DATA81_DUPLICATE_WITH_PAST_TTL,
                                           DELETABLE_CASE_DATA82_WITH_PAST_TTL);

    List<CaseData>  linkedCases3 = List.of(DELETABLE_CASE_DATA83_WITH_PAST_TTL);
}
