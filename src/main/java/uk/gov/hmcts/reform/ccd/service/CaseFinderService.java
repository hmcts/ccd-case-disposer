package uk.gov.hmcts.reform.ccd.service;

import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.policy.CaseTypeRetentionPolicyImpl;
import uk.gov.hmcts.reform.ccd.policy.RetentionPolicy;
import uk.gov.hmcts.reform.ccd.policy.TtlRetentionPolicyImpl;

import java.util.LinkedList;
import java.util.List;

@Named
@Slf4j
public class CaseFinderService {
    private final CaseFamilyService caseFamilyService;
    private final List<RetentionPolicy> retentionPolicies;

    public CaseFinderService(CaseFamilyService caseFamilyService, ParameterResolver parameterResolver) {
        this.caseFamilyService = caseFamilyService;
        retentionPolicies = List.of(
            new TtlRetentionPolicyImpl(),
            new CaseTypeRetentionPolicyImpl(parameterResolver)
        );
    }

    public List<CaseFamily> findCasesDueDeletion() {
        List<CaseFamily> deletableCaseFamilies = new LinkedList<>();
        for (CaseFamily caseFamily: caseFamilyService.getCaseFamilies()) {
            if (isAllCasesDeletable(caseFamily.linkedCases())) {
                deletableCaseFamilies.add(caseFamily);
            }
        }
        return deletableCaseFamilies;
    }

    private boolean isAllCasesDeletable(final List<CaseData> cases) {
        return cases.stream().noneMatch(this::isCaseRetainable);
    }

    private boolean isCaseRetainable(final CaseData caseData) {
        return retentionPolicies.stream()
            .map(policy -> policy.mustRetain(caseData))
            .filter(result -> result)
            .findFirst()
            .orElse(Boolean.FALSE);
    }
}
