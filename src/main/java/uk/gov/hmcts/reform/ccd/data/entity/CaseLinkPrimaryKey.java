package uk.gov.hmcts.reform.ccd.data.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
public class CaseLinkPrimaryKey implements Serializable {
    private Long caseId;
    private String caseTypeId;
}
