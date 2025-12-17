package uk.gov.hmcts.reform.ccd.util.log;

import jakarta.inject.Named;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Named
@Getter
public class TasksDeletionRecordHolder {

    private final ConcurrentMap<String, Integer> tasksDeletionResults = new ConcurrentHashMap<>();

    public void setCaseTasksDeletionResults(final String caseRef,
                                            final int caseTasksDeletionResults) {
        tasksDeletionResults.put(caseRef, caseTasksDeletionResults);
    }

    public int getTasksDeletionResults(final String caseRef) {
        return tasksDeletionResults.getOrDefault(caseRef, 0);
    }

    public Map<String, Integer> snapshot() {
        return Map.copyOf(tasksDeletionResults);
    }

    public void clear() {
        tasksDeletionResults.clear();
    }
}
