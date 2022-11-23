package uk.gov.hmcts.reform.ccd.service.remote;

import uk.gov.hmcts.reform.ccd.data.model.CaseData;

public interface DisposeRemoteOperation {
    void delete(final CaseData caseData) throws RuntimeException;
}