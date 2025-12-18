package uk.gov.hmcts.reform.ccd.util.log;

import jakarta.inject.Named;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Named
@Getter
public class HearingDeletionRecordHolder {
    private ConcurrentMap<String, Integer> hearingDeletionByCaseRef = new ConcurrentHashMap<>();

    public void setHearingDeletionResults(final String caseRef, final int deletionStatus) {
        hearingDeletionByCaseRef.put(caseRef, deletionStatus);
    }

    public int getHearingDeletionResults(final String caseRef) {
        return hearingDeletionByCaseRef.getOrDefault(caseRef, 0);
    }
}
