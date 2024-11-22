package uk.gov.hmcts.reform.ccd.util.log;

import jakarta.inject.Named;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Named
@Getter
public class HearingDeletionRecordHolder {
    private List<Map<String, Integer>> hearingDeletionRecordHolderList = new ArrayList<>();

    public void setHearingDeletionResults(final String caseRef,
                                          final int hearingDeletionStatus) {
        hearingDeletionRecordHolderList.add(Map.of(caseRef, hearingDeletionStatus));
    }

    public int getHearingDeletionResults(final String caseRef) {
        if (!hearingDeletionRecordHolderList.isEmpty()) {
            final Optional<Map<String, Integer>> deletionResultsMap =
                hearingDeletionRecordHolderList.stream()
                    .filter(hearingHolderEntry -> hearingHolderEntry.containsKey(caseRef))
                    .findFirst();
            if (deletionResultsMap.isPresent()) {
                return deletionResultsMap.get().get(caseRef);
            }
        }
        return 0;
    }
}
