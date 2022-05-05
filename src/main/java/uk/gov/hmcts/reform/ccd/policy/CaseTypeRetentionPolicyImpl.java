package uk.gov.hmcts.reform.ccd.policy;

import lombok.NonNull;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

public class CaseTypeRetentionPolicyImpl implements RetentionPolicy {
    private final ParameterResolver parameterResolver;

    public CaseTypeRetentionPolicyImpl(final ParameterResolver parameterResolver) {
        this.parameterResolver = parameterResolver;
    }

    @Override
    public Boolean mustRetain(@NonNull final CaseData caseData) {
        return !isDeletableCaseType(caseData.getCaseType());
    }

    private Boolean isDeletableCaseType(@NonNull final String caseType) {
        return parameterResolver.getAllDeletableCaseTypes().contains(caseType);
    }

}
