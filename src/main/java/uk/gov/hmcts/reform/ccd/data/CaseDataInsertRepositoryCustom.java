package uk.gov.hmcts.reform.ccd.data;

import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

public interface CaseDataInsertRepositoryCustom {

    void saveCaseData(CaseDataEntity caseDataEntity);
}
