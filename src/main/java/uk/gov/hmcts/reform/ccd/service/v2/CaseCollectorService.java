package uk.gov.hmcts.reform.ccd.service.v2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.util.perf.LogExecutionTime;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class CaseCollectorService {

    private final CaseDataRepository caseDataRepository;
    private final CaseLinkRepository caseLinkRepository;

    @LogExecutionTime("Collect deletable cases")
    public Set<CaseData> getDeletableCases(List<String> caseTypes) {
        if (caseTypes == null || caseTypes.isEmpty()) {
            return new HashSet<>();
        }
        List<CaseData> expiredCases = getExpiredCases(caseTypes);
        Set<Long> expiredIds = expiredCases.stream().map(CaseData::getId).collect(Collectors.toUnmodifiableSet());

        List<CaseLinkEntity> links = caseLinkRepository.findExpiredCaseLinksByCaseTypes(caseTypes);

        UnionFind groups = new UnionFind(expiredIds);
        mergeExpiredLinks(groups, links, expiredIds);
        Set<Long> blockedGroupRoots = findBlockedGroups(groups, links, expiredIds);

        return filterByBlockedGroups(expiredCases, groups, blockedGroupRoots);
    }

    private void mergeExpiredLinks(UnionFind groups, List<CaseLinkEntity> links, Set<Long> expiredIds) {
        for (CaseLinkEntity link: links) {
            Long left = link.getCaseId();
            Long right = link.getLinkedCaseId();
            if (expiredIds.contains(left) && expiredIds.contains(right)) {
                groups.merge(left, right);
            }
        }
    }

    private Set<Long> findBlockedGroups(UnionFind groups, List<CaseLinkEntity> links, Set<Long> expiredIds) {
        Set<Long> blockedRoots = new HashSet<>();
        for (CaseLinkEntity link: links) {
            Long left = link.getCaseId();
            Long right = link.getLinkedCaseId();
            boolean leftExpired = expiredIds.contains(left);
            boolean rightExpired = expiredIds.contains(right);
            if (leftExpired && !rightExpired) {
                blockedRoots.add(groups.findRoot(left));
            }
            if (rightExpired && !leftExpired) {
                blockedRoots.add(groups.findRoot(right));
            }
        }
        return blockedRoots;
    }

    private Set<CaseData> filterByBlockedGroups(List<CaseData> expiredCases, UnionFind groups, Set<Long> blockedIds) {
        return expiredCases.stream()
            .filter(caseData -> !blockedIds.contains(groups.findRoot(caseData.getId())))
            .collect(Collectors.toSet());
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

    private static final class UnionFind {
        private final Map<Long, Long> rootByCaseId = new ConcurrentHashMap<>();

        UnionFind(Set<Long> caseIds) {
            for (Long caseId : caseIds) {
                rootByCaseId.put(caseId, caseId);
            }
        }

        Long findRoot(Long caseId) {
            Long parent = Objects.requireNonNull(rootByCaseId.get(caseId), "Unknown case id " + caseId);
            if (!parent.equals(caseId)) {
                parent = findRoot(parent);
                rootByCaseId.put(caseId, parent);
            }
            return parent;
        }

        void merge(Long left, Long right) {
            Long firstRoot = findRoot(left);
            Long secondRoot = findRoot(right);
            if (!firstRoot.equals(secondRoot)) {
                rootByCaseId.put(secondRoot, firstRoot);
            }
        }
    }
}
