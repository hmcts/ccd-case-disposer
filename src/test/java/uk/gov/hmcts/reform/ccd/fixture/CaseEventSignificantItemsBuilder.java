package uk.gov.hmcts.reform.ccd.fixture;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventSignificantItemsEntity;

@AllArgsConstructor
public class CaseEventSignificantItemsBuilder {
    private final Long id;
    private final Long caseEventId;

    public CaseEventSignificantItemsEntity build() {
        CaseEventSignificantItemsEntity caseEventSignificantItemsEntity = new CaseEventSignificantItemsEntity();
        caseEventSignificantItemsEntity.setId(id);
        caseEventSignificantItemsEntity.setCaseEventId(caseEventId);
        return caseEventSignificantItemsEntity;
    }
}
