package uk.gov.hmcts.reform.ccd.service.remote;

import uk.gov.hmcts.reform.ccd.data.model.CaseData;

@FunctionalInterface
public interface DisposeRemoteOperation {

    void delete(CaseData caseData);
}
