package uk.gov.hmcts.reform.ccd.config;

import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.policy.CaseTypeRetentionPolicyImpl;
import uk.gov.hmcts.reform.ccd.policy.RetentionPolicy;
import uk.gov.hmcts.reform.ccd.policy.TtlRetentionPolicyImpl;

import java.util.List;

public class RetentionPolicyConfiguration {
    @Bean
    public List<RetentionPolicy> provideRetentionPolicies(final ParameterResolver parameterResolver) {
        return List.of(new TtlRetentionPolicyImpl(), new CaseTypeRetentionPolicyImpl(parameterResolver));
    }
}
