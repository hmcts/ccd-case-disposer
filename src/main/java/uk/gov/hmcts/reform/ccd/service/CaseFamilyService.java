package uk.gov.hmcts.reform.ccd.service;

import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.ccd.data.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

@Named
@RequiredArgsConstructor
public class CaseFamilyService {

    private final CaseDataRepository caseDataRepository;
    private final CaseLinkRepository caseLinkRepository;
    private final ParameterResolver parameterResolver;

    public List<CaseFamily> getCaseFamilies() {
        final List<CaseDataEntity> expiredCases = getExpiredCases();
        final Set<Long> allCaseIds = new HashSet<>();

        final List<Set<Long>> linkedCasesGroup = new LinkedList<>();

        for (final CaseDataEntity caseDataEntity : expiredCases) {
            if (allCaseIds.contains(caseDataEntity.getId())) {
                continue;
            }
            final Set<Long> linkedCases = findLinkedCases(caseDataEntity);
            allCaseIds.addAll(linkedCases);
            linkedCasesGroup.add(linkedCases);
        }
        return linkedCasesGroup.stream()
                .map(this::buildCaseFamily)
                .toList();
    }

    private List<CaseDataEntity> getExpiredCases() {
        return caseDataRepository.findExpiredCases(parameterResolver.getAllDeletableCaseTypes());
    }

    private Set<Long> findLinkedCases(final CaseDataEntity caseDataEntity) {
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();

        Long startCaseId = caseDataEntity.getId();
        visited.add(startCaseId);
        queue.add(startCaseId);

        while (!queue.isEmpty()) {
            Long caseId = queue.poll();

            for (CaseLinkEntity caseLinkEntity : caseLinkRepository.findByCaseIdOrLinkedCaseId(caseId)) {
                Long leftCaseId = caseLinkEntity.getCaseId();
                Long rightCaseId = caseLinkEntity.getLinkedCaseId();
                if (visited.add(leftCaseId)) { // add returns true if it wasn't already present
                    queue.add(leftCaseId);
                }
                if (visited.add(rightCaseId)) {
                    queue.add(rightCaseId);
                }
            }
        }
        return visited;
    }

    private CaseFamily buildCaseFamily(Set<Long> caseIds) {
        final List<CaseData> caseDataList = caseDataRepository.findAllById(caseIds)
                .stream()
                .map(CaseData::fromEntity)
                .toList();
        return new CaseFamily(caseDataList);
    }
}
