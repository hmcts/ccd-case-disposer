package uk.gov.hmcts.reform.ccd.util.log;

import jakarta.inject.Named;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Named
@Getter
public class RoleDeletionRecordHolder {

    private final ConcurrentMap<String, Integer> roleDeletionResults =
        new ConcurrentHashMap<>();

    public void setCaseRolesDeletionResults(final String caseRef,
                                            final int caseRolesDeletionResults) {
        roleDeletionResults.put(caseRef, caseRolesDeletionResults);
    }

    public int getCaseRolesDeletionResults(final String caseRef) {
        return roleDeletionResults.getOrDefault(caseRef, 0);
    }

    public Map<String, Integer> snapshot() {
        return Map.copyOf(roleDeletionResults);
    }

    public void clear() {
        roleDeletionResults.clear();
    }
}
