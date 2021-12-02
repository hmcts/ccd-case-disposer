package uk.gov.hmcts.reform.ccd.policy;

import uk.gov.hmcts.reform.ccd.data.model.CaseData;

public interface RetentionPolicy {
    Boolean mustRetain(CaseData caseDataEntity);
}
