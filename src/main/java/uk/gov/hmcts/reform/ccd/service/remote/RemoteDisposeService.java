package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.util.List;

@Service
@Slf4j
public class RemoteDisposeService {

    @Autowired
    private List<DisposeRemoteOperation> disposeRemoteOperations;

    /**
     * Each class that implements DisposeRemoteOperation is responsible for handling the deletion logic.
     * The implementation will decide whether the deletion should take place or not, based on the provided CaseData.
     */
    public void remoteDeleteAll(final CaseData caseData) {
        Stopwatch timer = Stopwatch.createUnstarted();
        disposeRemoteOperations.forEach(disposeRemoteOperation -> {
            timer.start();
            disposeRemoteOperation.delete(caseData);
            log.debug(
                "Performance: {} for case {} took {}",
                disposeRemoteOperation,
                caseData.getReference(),
                timer.stop());
            timer.reset();
        });
    }
}
