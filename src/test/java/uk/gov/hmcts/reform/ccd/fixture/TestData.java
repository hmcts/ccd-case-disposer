package uk.gov.hmcts.reform.ccd.fixture;

import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.time.LocalDate;
import java.util.List;

public interface TestData {
    String CASE_TYPE = "aaa";
    String INDEX_NAME_PATTERN = "%s_cases";

    LocalDate TODAY = LocalDate.now();
    LocalDate YESTERDAY = TODAY.minusDays(1L);
    LocalDate TOMORROW = TODAY.plusDays(1L);

    CaseDataEntity CASE_DATA_YESTERDAY = new CaseDataEntityBuilder(1L)
        .withReference(1L)
        .withCaseType(CASE_TYPE)
        .withResolvedTtl(YESTERDAY)
        .build();

    CaseDataEntity CASE_DATA_TODAY = new CaseDataEntityBuilder(2L)
        .withReference(2L)
        .withCaseType(CASE_TYPE)
        .withResolvedTtl(TODAY)
        .build();

    CaseDataEntity CASE_DATA_TOMORROW = new CaseDataEntityBuilder(3L)
        .withReference(3L)
        .withCaseType(CASE_TYPE)
        .withResolvedTtl(TOMORROW)
        .build();

    List<CaseDataEntity> CASE_DATA_ENTITIES =
        List.of(CASE_DATA_YESTERDAY, CASE_DATA_TODAY, CASE_DATA_TOMORROW);

    CaseDataEntity LINKED_CASE_DATA_10 = new CaseDataEntityBuilder(10L)
        .withReference(10L)
        .withCaseType(CASE_TYPE)
        .withResolvedTtl(YESTERDAY)
        .build();

    CaseDataEntity LINKED_CASE_DATA_11 = new CaseDataEntityBuilder(11L)
        .withReference(11L)
        .withCaseType(CASE_TYPE)
        .withResolvedTtl(YESTERDAY)
        .build();
}
