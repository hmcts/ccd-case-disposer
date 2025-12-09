package uk.gov.hmcts.reform.ccd.service.remote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class RemoteDisposeService {

    @Autowired
    private List<DisposeRemoteOperation> disposeRemoteOperations;

    /**
     * Each class that implements DisposeRemoteOperation is responsible for handling the deletion logic.
     * The implementation will decide whether the deletion should take place or not, based on the provided CaseData.
     */
    public CompletableFuture<Void> remoteDeleteAll(CaseData caseData) {
        List<CompletableFuture<Void>> futures = disposeRemoteOperations
            .stream()
            .map(op -> op.delete(caseData))
            .toList();
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
