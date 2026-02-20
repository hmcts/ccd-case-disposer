package uk.gov.hmcts.reform.ccd.util.log;

import jakarta.inject.Named;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Named
@Getter
public class RoleDeletionRecordHolder {
    private List<Map<String, Integer>> roleDeletionRecordHolderList = Collections.synchronizedList(new ArrayList<>());

    public void setCaseRolesDeletionResults(final String caseRef,
                                                final int caseRolesDeletionResults) {
        roleDeletionRecordHolderList.add(Map.of(caseRef, caseRolesDeletionResults));
    }

    public int getCaseRolesDeletionResults(final String caseRef) {
        if (!roleDeletionRecordHolderList.isEmpty()) {
            final Optional<Map<String, Integer>> deletionResultsMap =
                roleDeletionRecordHolderList.stream()
                    .filter(roleHolderEntry -> roleHolderEntry.containsKey(caseRef))
                    .findFirst();
            if (deletionResultsMap.isPresent()) {
                return deletionResultsMap.get().get(caseRef);
            }
        }
        return 0;
    }
}
