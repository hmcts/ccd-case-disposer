package uk.gov.hmcts.reform.ccd.service;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.data.model.Retainability;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.policy.CaseTypeRetentionPolicyImpl;
import uk.gov.hmcts.reform.ccd.policy.RetentionPolicy;
import uk.gov.hmcts.reform.ccd.policy.TtlRetentionPolicyImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;

import static uk.gov.hmcts.reform.ccd.util.ListUtil.distinctByKey;

@Named
@Slf4j
public class CaseFinderService {
    private final ParameterResolver parameterResolver;
    private final CaseFamilyTreeService caseFamilyTreeService;

    static final BiFunction<Set<Long>, Set<Long>, Set<Long>> INTERSECTION_FUNCTION = (setX, setY) -> {
        Set<Long> x = new HashSet<>(setX);
        Set<Long> y = new HashSet<>(setY);

        return x.retainAll(y) ? Collections.unmodifiableSet(x) : setX;
    };

    @Inject
    public CaseFinderService(final ParameterResolver parameterResolver,
                             final CaseFamilyTreeService caseFamilyTreeService) {
        this.parameterResolver = parameterResolver;
        this.caseFamilyTreeService = caseFamilyTreeService;
    }

    public List<CaseFamily> findCasesDueDeletion() {
        final List<CaseFamily> caseFamilies = caseFamilyTreeService.getCaseFamilies();

        return getDeletableCases(caseFamilies);
    }

    private boolean isCaseRetainable(final CaseData caseData) {
        final List<RetentionPolicy> retentionPolicies = List.of(
            new TtlRetentionPolicyImpl(),
            new CaseTypeRetentionPolicyImpl(parameterResolver)
        );
        return retentionPolicies.stream()
            .map(policy -> policy.mustRetain(caseData))
            .filter(result -> result)
            .findFirst()
            .orElse(Boolean.FALSE);
    }

    private boolean isCaseFamilyRetainable(final CaseFamily caseFamily) {
        return Stream.concat(
                Stream.of(caseFamily.getRootCase()),
                caseFamily.getLinkedCases().stream()
            )
            .anyMatch(this::isCaseRetainable);
    }

    Map<Enum<Retainability>, List<CaseFamily>> partitionByRetainability(final List<CaseFamily> caseFamilies) {
        final Map<Boolean, List<CaseFamily>> partitioned = caseFamilies.stream()
            .collect(Collectors.partitioningBy(this::isCaseFamilyRetainable));

        return Map.of(Retainability.RETAIN, partitioned.get(Boolean.TRUE),
                      Retainability.INDETERMINATE, partitioned.get(Boolean.FALSE)
        );
    }

    private Set<Long> getCaseFamilyCaseIds(final CaseFamily caseFamily) {
        return Stream.concat(
                Stream.of(caseFamily.getRootCase()),
                caseFamily.getLinkedCases().stream()
            )
            .map(CaseData::getId)
            .collect(Collectors.toUnmodifiableSet());
    }

    private Set<Long> getCaseFamiliesToRetainCaseIds(final List<CaseFamily> caseFamilies) {
        return caseFamilies.stream()
            .map(this::getCaseFamilyCaseIds)
            .flatMap(Collection::stream)
            .collect(Collectors.toUnmodifiableSet());
    }

    List<CaseFamily> findRetainableCaseFamilies(final List<CaseFamily> indeterminateCaseFamilies,
                                                final List<CaseFamily> caseFamiliesToRetain) {

        final LinkedList<CaseFamily> stagingQueue = new LinkedList<>(indeterminateCaseFamilies);
        final LinkedList<CaseFamily> holdingQueue = new LinkedList<>();
        final List<CaseFamily> retentionList = new ArrayList<>(caseFamiliesToRetain);

        while (!stagingQueue.isEmpty()) {
            final CaseFamily caseFamily = stagingQueue.poll();
            final Set<Long> caseFamilyCaseIds = getCaseFamilyCaseIds(caseFamily);
            final Set<Long> caseFamiliesToRetainCaseIds = getCaseFamiliesToRetainCaseIds(retentionList);
            final Set<Long> intersection = INTERSECTION_FUNCTION.apply(caseFamilyCaseIds, caseFamiliesToRetainCaseIds);
            if (intersection.isEmpty()) {
                holdingQueue.add(caseFamily);
            } else {
                retentionList.add(caseFamily);
                stagingQueue.addAll(holdingQueue);
                holdingQueue.clear();
            }
        }

        return retentionList.stream()
            .sorted(Comparator.comparing(caseFamily -> caseFamily.getRootCase().getId()))
            .collect(Collectors.toUnmodifiableList());
    }

    private List<CaseFamily> getDeletableCases(final List<CaseFamily> caseFamilies) {
        final Map<Enum<Retainability>, List<CaseFamily>> partitioned = partitionByRetainability(caseFamilies);

        final List<CaseFamily> caseFamiliesToRetain = findRetainableCaseFamilies(
            partitioned.get(Retainability.INDETERMINATE),
            partitioned.get(Retainability.RETAIN)
        );
        final Set<Long> caseFamiliesToRetainCaseIds = getCaseFamiliesToRetainCaseIds(caseFamiliesToRetain);

        return caseFamilies.stream()
            .filter(distinctByKey(caseFamily -> caseFamily.getRootCase().getId()))
            .filter(caseFamily -> !caseFamiliesToRetainCaseIds.contains(caseFamily.getRootCase().getId()))
            .collect(Collectors.toUnmodifiableList());
    }

}
