package uk.gov.hmcts.reform.ccd.service.v2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class CaseCollectorService {

    private final CaseDataRepository caseDataRepository;
    private final CaseLinkRepository caseLinkRepository;

    public Set<CaseData> getDeletableCases(List<String> caseTypes) {
        if (caseTypes == null || caseTypes.isEmpty()) {
            return new HashSet<>();
        }
        List<CaseData> expiredCases = getExpiredCases(caseTypes);
        Set<CaseData> deletableCases = new HashSet<>();
        Set<Long> expiredIds = expiredCases.stream().map(CaseData::getId).collect(Collectors.toUnmodifiableSet());
        for (CaseData caseData : expiredCases) {
            if (areAllLinksDeletable(caseData.getId(), expiredIds)) {
                deletableCases.add(caseData);
            }
        }
        return deletableCases;
    }

    private boolean areAllLinksDeletable(Long caseId, Set<Long> expiredIds) {
        Set<Long> visited = new HashSet<>();
        Set<Long> queue = new HashSet<>();

        visited.add(caseId);
        queue.add(caseId);

        String dbgMsg = "Case id {} cannot be removed because it is linked by non-deletable case - {}";

        while (!queue.isEmpty()) {
            List<CaseLinkEntity> links = caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(queue);
            queue.clear();
            for (CaseLinkEntity caseLinkEntity : links) {
                Long leftCaseId = caseLinkEntity.getCaseId();
                Long rightCaseId = caseLinkEntity.getLinkedCaseId();

                // if even one linked case is not deletable, we don't delete the case
                if (!expiredIds.contains(leftCaseId)) {
                    log.debug(dbgMsg, caseId, leftCaseId);
                    return false;
                }

                if (!expiredIds.contains(rightCaseId)) {
                    log.debug(dbgMsg, caseId, rightCaseId);
                    return false;
                }

                // add returns true if it wasn't already present
                if (visited.add(leftCaseId)) {
                    queue.add(leftCaseId);
                }
                if (visited.add(rightCaseId)) {
                    queue.add(rightCaseId);
                }
            }
        }

        log.debug("{} unique linked deletable cases found", visited.size());
        return true;
    }

    private List<CaseData> getExpiredCases(List<String> caseTypes) {
        List<CaseDataEntity> entities = caseDataRepository.findExpiredCases(caseTypes);
        List<CaseData> cases = entities.stream().map(entity -> CaseData.builder()
                .id(entity.getId())
                .reference(entity.getReference())
                .caseType(entity.getCaseType())
                .jurisdiction(entity.getJurisdiction())
                .resolvedTtl(entity.getResolvedTtl())
                .build()).toList();

        log.debug("Cases with expired TTL: {}", cases.size());
        cases.forEach(cs -> log.debug(
            "Case id: {} ref: {} type: {}, ttl: {}",
            cs.getId(),
            cs.getReference(),
            cs.getCaseType(),
            cs.getResolvedTtl()));

        return cases;
    }

}
