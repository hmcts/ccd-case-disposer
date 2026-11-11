package uk.gov.hmcts.reform.ccd.fixture;

import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.time.LocalDate;

@SuppressWarnings({"java:S8692", "PMD.ConstantsInInterface"})
public interface TestData {
    String DELETABLE_CASE_TYPE = "deletable_case_type";
    String JURISDICTION = "deletable_jurisdiction";
    String NON_DELETABLE_CASE_TYPE = "non_deletable_case_type";
    String DELETABLE_CASE_TYPE_SIMULATION = "deletable_case_type_simulation";
    String STATE = "case_state";
    String INDEX_NAME_PATTERN = "%s_cases";

    LocalDate TODAY = LocalDate.now();
    LocalDate YESTERDAY = TODAY.minusDays(1L);
    CaseDataEntity DELETABLE_CASE_ENTITY_WITH_PAST_TTL = new CaseDataEntityBuilder(1L)
            .withReference(1L)
            .withCaseType(DELETABLE_CASE_TYPE)
            .withResolvedTtl(YESTERDAY)
            .build();
    CaseData DELETABLE_CASE_DATA_WITH_PAST_TTL = new CaseData(1L, 1L, DELETABLE_CASE_TYPE,
            JURISDICTION, STATE, YESTERDAY, 1L, null);
    CaseData DELETABLE_CASE_DATA4_WITH_PAST_TTL = new CaseData(4L, 4L, DELETABLE_CASE_TYPE,
            JURISDICTION, STATE, YESTERDAY, 4L, null);
    CaseData DELETABLE_CASE_DATA5_WITH_PAST_TTL = new CaseData(5L, 5L, DELETABLE_CASE_TYPE,
            JURISDICTION, STATE, YESTERDAY, 5L, null);

    CaseData DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_1 = new CaseData(30L,
            30L,
            DELETABLE_CASE_TYPE_SIMULATION,
            JURISDICTION,
            STATE,
            YESTERDAY,
            30L,
            null);
    CaseData DELETABLE_CASE_DATA_WITH_PAST_TTL_SIMULATION_2 = new CaseData(31L,
            31L,
            DELETABLE_CASE_TYPE_SIMULATION,
            JURISDICTION,
            STATE,
            YESTERDAY,
            31L,
            null);
}
