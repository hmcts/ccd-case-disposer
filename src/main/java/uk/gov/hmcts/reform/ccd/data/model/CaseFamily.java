package uk.gov.hmcts.reform.ccd.data.model;

import lombok.Value;

import java.util.List;

@Value
public class CaseFamily {
    CaseData rootCase;
    List<CaseData> linkedCases;
}
