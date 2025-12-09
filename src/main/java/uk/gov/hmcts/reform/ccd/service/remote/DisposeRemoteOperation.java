package uk.gov.hmcts.reform.ccd.service.remote;

import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.util.concurrent.CompletableFuture;

public interface DisposeRemoteOperation {

    CompletableFuture<Void> delete(final CaseData caseData);
}
