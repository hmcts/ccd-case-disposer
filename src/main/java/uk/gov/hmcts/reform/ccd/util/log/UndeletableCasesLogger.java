package uk.gov.hmcts.reform.ccd.util.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.policy.TtlRetentionPolicyImpl;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UndeletableCasesLogger {

    public static final String MSG_WITH_LINKS =
            "Not deleting case ref: {} due to existing link to case(s): {}";
    public static final String MSG_WITHOUT_LINKS =
            "Not deleting case ref: {} due to indirectly linked non-deletable case";

    private final ProcessedCasesRecordHolder casesRecordHolder;

    public void logUndeletableCases(List<CaseFamily> caseFamilies, List<CaseFamily> deletableCaseFamilies) {
        Map<Long, CaseFamily> caseFamiliesMap = caseFamilies.stream().collect(
            Collectors.toMap(
                caseFamily -> caseFamily.getRootCase().getId(),
                caseFamily -> caseFamily,
                (existing, incoming) -> existing // in case of duplicates, keep the existing one
            )
        );

        Set<Long> caseFamiliesRootIds = caseFamiliesMap.keySet();

        Set<Long> deletableCaseFamiliesRootIds = deletableCaseFamilies.stream()
            .map(caseFamily -> caseFamily.getRootCase().getId()).collect(Collectors.toSet());
        Set<Long> undeletableCaseFamiliesRootIds = new HashSet<>(caseFamiliesRootIds);
        undeletableCaseFamiliesRootIds.removeAll(deletableCaseFamiliesRootIds);

        if (!undeletableCaseFamiliesRootIds.isEmpty()) {
            logNonDeletableCaseFamilies(caseFamiliesMap, new LinkedList<>(undeletableCaseFamiliesRootIds));
        }

    }

    private void logNonDeletableCaseFamilies(Map<Long, CaseFamily> caseFamilies, List<Long> undeletableRootIds) {
        final TtlRetentionPolicyImpl ttlRetentionPolicy = new TtlRetentionPolicyImpl();

        undeletableRootIds.forEach(rootId -> {
            CaseFamily caseFamily = caseFamilies.get(rootId);
            List<CaseData> cases = new ArrayList<>();
            cases.add(caseFamily.getRootCase());
            cases.addAll(caseFamily.getLinkedCases());

            List<CaseData> blockers = cases.stream()
                .filter(ttlRetentionPolicy::mustRetain)
                .toList();

            cases.forEach(caseData -> {
                casesRecordHolder.addNonDeletableCase(caseData);
                if (!blockers.contains(caseData)) {
                    if (blockers.isEmpty()) {
                        log.info(MSG_WITHOUT_LINKS, caseData.getReference());
                    } else {
                        log.info(MSG_WITH_LINKS, caseData.getReference(), blockers);
                    }
                }
            });
        });
    }
}
