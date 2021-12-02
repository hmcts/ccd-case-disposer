package uk.gov.hmcts.reform.ccd.data.model;

import lombok.Value;

import java.time.LocalDate;

@Value
public class CaseData {
    Long id;
    Long reference;
    String caseType;
    LocalDate resolvedTtl;
    CaseData parentCase;
}
