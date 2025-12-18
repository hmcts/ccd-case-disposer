package uk.gov.hmcts.reform.ccd.util.log;

import jakarta.inject.Named;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Named
@Getter
public class RoleDeletionRecordHolder {
    private ConcurrentMap<String, Integer> roleDeletionByCaseRef = new ConcurrentHashMap<>();

    public void setCaseRolesDeletionResults(final String caseRef, final int rolesDeletionResult) {
        roleDeletionByCaseRef.put(caseRef, rolesDeletionResult);
    }

    public int getCaseRolesDeletionResults(final String caseRef) {
        return roleDeletionByCaseRef.getOrDefault(caseRef, 0);
    }
}
