package uk.gov.hmcts.reform.ccd.data.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CaseLinkPrimaryKey implements Serializable {
    private Long caseId;
    private Long linkedCaseId;
}
