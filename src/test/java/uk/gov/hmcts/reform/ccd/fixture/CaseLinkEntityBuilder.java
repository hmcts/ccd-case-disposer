package uk.gov.hmcts.reform.ccd.fixture;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;

@AllArgsConstructor
public class CaseLinkEntityBuilder {
    private final Long caseId;
    private final String caseTypeId;
    private final Long linkedCaseId;

    public CaseLinkEntity build() {
        CaseLinkEntity caseLinkEntity = new CaseLinkEntity();
        caseLinkEntity.setCaseId(caseId);
        caseLinkEntity.setCaseTypeId(caseTypeId);
        caseLinkEntity.setLinkedCaseId(linkedCaseId);

        return caseLinkEntity;
    }
}
