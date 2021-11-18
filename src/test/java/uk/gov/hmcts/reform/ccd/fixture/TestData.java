package uk.gov.hmcts.reform.ccd.fixture;

import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.time.LocalDate;
import java.util.List;

public interface TestData {
    String DELETABLE_CASE_TYPE = "deletable_case_type";
    String NON_DELETABLE_CASE_TYPE = "non_deletable_case_type";
    String INDEX_NAME_PATTERN = "%s_cases";

    LocalDate TODAY = LocalDate.now();
    LocalDate YESTERDAY = TODAY.minusDays(1L);
    LocalDate TOMORROW = TODAY.plusDays(1L);

    CaseDataEntity DELETABLE_CASE_WITH_PAST_TTL = new CaseDataEntityBuilder(1L)
        .withReference(1L)
        .withCaseType(DELETABLE_CASE_TYPE)
        .withResolvedTtl(YESTERDAY)
        .build();

    CaseDataEntity DELETABLE_CASE2_WITH_PAST_TTL = new CaseDataEntityBuilder(1000L)
        .withReference(1000L)
        .withCaseType(DELETABLE_CASE_TYPE)
        .withResolvedTtl(YESTERDAY)
        .build();

    CaseDataEntity DELETABLE_CASE_WITH_TODAY_TTL = new CaseDataEntityBuilder(2L)
        .withReference(2L)
        .withCaseType(DELETABLE_CASE_TYPE)
        .withResolvedTtl(TODAY)
        .build();

    CaseDataEntity DELETABLE_CASE_WITH_FUTURE_TTL = new CaseDataEntityBuilder(3L)
        .withReference(3L)
        .withCaseType(DELETABLE_CASE_TYPE)
        .withResolvedTtl(TOMORROW)
        .build();

    CaseDataEntity NON_DELETABLE_CASE_WITH_TODAY_TTL = new CaseDataEntityBuilder(100L)
        .withReference(100L)
        .withCaseType(NON_DELETABLE_CASE_TYPE)
        .withResolvedTtl(TODAY)
        .build();

    List<CaseDataEntity> CASE_DATA_ENTITIES =
        List.of(DELETABLE_CASE_WITH_PAST_TTL, DELETABLE_CASE_WITH_TODAY_TTL, DELETABLE_CASE_WITH_FUTURE_TTL);

    CaseDataEntity LINKED_CASE_DATA_10 = new CaseDataEntityBuilder(10L)
        .withReference(10L)
        .withCaseType(DELETABLE_CASE_TYPE)
        .withResolvedTtl(YESTERDAY)
        .build();

    CaseDataEntity LINKED_CASE_DATA_11 = new CaseDataEntityBuilder(11L)
        .withReference(11L)
        .withCaseType(DELETABLE_CASE_TYPE)
        .withResolvedTtl(YESTERDAY)
        .build();

    CaseDataEntity LINKED_CASE_DATA_12 = new CaseDataEntityBuilder(12L)
        .withReference(12L)
        .withCaseType(DELETABLE_CASE_TYPE)
        .withResolvedTtl(YESTERDAY)
        .build();

    CaseDataEntity LINKED_CASE_DATA_100 = new CaseDataEntityBuilder(100L)
        .withReference(100L)
        .withCaseType(NON_DELETABLE_CASE_TYPE)
        .withResolvedTtl(YESTERDAY)
        .build();

    CaseDataEntity LINKED_CASE_DATA_101 = new CaseDataEntityBuilder(101L)
        .withReference(101L)
        .withCaseType(DELETABLE_CASE_TYPE)
        .withResolvedTtl(TOMORROW)
        .build();
}
