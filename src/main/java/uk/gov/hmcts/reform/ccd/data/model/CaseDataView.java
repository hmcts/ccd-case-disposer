package uk.gov.hmcts.reform.ccd.data.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CaseDataView {
    private String caseType;
    private Long caseRef;
    private String state;
}
