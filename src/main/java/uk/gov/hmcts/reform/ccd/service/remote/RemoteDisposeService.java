package uk.gov.hmcts.reform.ccd.service.remote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.util.List;

@Service
public class RemoteDisposeService {

    @Autowired
    private List<DisposeRemoteOperation> disposeRemoteOperations;

    /**
     * Each class that implements DisposeRemoteOperation is responsible for handling the deletion logic.
     * The implementation will decide whether the deletion should take place or not, based on the provided CaseData.
     */
    public void remoteDeleteAll(final CaseData caseData) {
        disposeRemoteOperations.forEach(disposeRemoteOperation -> disposeRemoteOperation.delete(caseData));
    }
}
