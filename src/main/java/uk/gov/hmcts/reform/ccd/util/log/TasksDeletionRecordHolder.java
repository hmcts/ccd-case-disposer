package uk.gov.hmcts.reform.ccd.util.log;

import jakarta.inject.Named;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Named
@Getter
public class TasksDeletionRecordHolder {

    private ConcurrentMap<String, Integer> tasksDeletionByCaseRef = new ConcurrentHashMap<>();

    public void setCaseTasksDeletionResults(final String caseRef, final int deletionResult) {
        tasksDeletionByCaseRef.put(caseRef, deletionResult);
    }

    public int getTasksDeletionResults(final String caseRef) {
        return tasksDeletionByCaseRef.getOrDefault(caseRef, 0);
    }
}
