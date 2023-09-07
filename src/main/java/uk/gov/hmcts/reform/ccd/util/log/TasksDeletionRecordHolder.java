package uk.gov.hmcts.reform.ccd.util.log;

import jakarta.inject.Named;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Named
@Getter
public class TasksDeletionRecordHolder {
    private List<Map<String, Integer>> tasksDeletionRecordHolderList = new ArrayList<>();

    public void setCaseTasksDeletionResults(final String caseRef,
                                            final int caseTasksDeletionResults) {
        tasksDeletionRecordHolderList.add(Map.of(caseRef, caseTasksDeletionResults));
    }

    public int getTasksDeletionResults(final String caseRef) {
        if (!tasksDeletionRecordHolderList.isEmpty()) {
            final Optional<Map<String, Integer>> deletionResultsMap =
                tasksDeletionRecordHolderList.stream()
                    .filter(taskHolderEntry -> taskHolderEntry.containsKey(caseRef))
                    .findFirst();
            if (deletionResultsMap.isPresent()) {
                return deletionResultsMap.get().get(caseRef);
            }
        }
        return 0;
    }
}
