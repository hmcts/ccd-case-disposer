package uk.gov.hmcts.reform.ccd.data.model;

import lombok.Value;

import java.time.LocalDate;

@Value
public class CaseData {
    Long id;
    Long reference;
    String caseType;
    String jurisdiction;
    LocalDate resolvedTtl;
    Long familyId;
    CaseData parentCase;
}
