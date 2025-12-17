package uk.gov.hmcts.reform.ccd.util.log;

import jakarta.inject.Named;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Named
@Getter
public class HearingDeletionRecordHolder {

    private final ConcurrentMap<String, Integer> hearingDeletionRecordHolderList =
        new ConcurrentHashMap<>();

    public void setHearingDeletionResults(final String caseRef,
                                          final int hearingDeletionStatus) {
        hearingDeletionRecordHolderList.put(caseRef, hearingDeletionStatus);
    }

    public int getHearingDeletionResults(final String caseRef) {
        return hearingDeletionRecordHolderList.getOrDefault(caseRef, 0);
    }

    public Map<String, Integer> snapshot() {
        return Map.copyOf(hearingDeletionRecordHolderList);
    }

    public void clear() {
        hearingDeletionRecordHolderList.clear();
    }
}
