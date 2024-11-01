package uk.gov.hmcts.reform.ccd.service.remote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.util.List;

import static org.springframework.aop.support.AopUtils.getTargetClass;

@Service
public class RemoteDisposeService {

    @Autowired
    private List<DisposeRemoteOperation> disposeRemoteOperations;

    private static final String HEARING_RECORDINGS_CASE_TYPE = "HearingRecordings";

    public void remoteDeleteAll(final CaseData caseData) {
        disposeRemoteOperations.forEach(operation -> {
            boolean isHearingCase = caseData.getCaseType().equals(HEARING_RECORDINGS_CASE_TYPE);

            boolean isInstanceOfDisposeDocuments = getTargetClass(operation)
                .equals(DisposeDocumentsRemoteOperation.class);

            boolean isInstanceOfDisposeHearings = getTargetClass(operation)
                .equals(DisposeHearingsRemoteOperation.class);

            if ((!isHearingCase && isInstanceOfDisposeDocuments)
                || (isHearingCase && isInstanceOfDisposeHearings)
                || (!isInstanceOfDisposeDocuments && !isInstanceOfDisposeHearings)) {
                operation.delete(caseData);
            }
        });
    }
}
