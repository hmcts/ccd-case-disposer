package uk.gov.hmcts.reform.ccd.data.model;

import lombok.Value;

import java.util.List;

@Value
public class CaseData {
    Long id;
    Long reference;
    String caseType;
    List<Long> linkedCases;
    RetentionStatus status;
}
