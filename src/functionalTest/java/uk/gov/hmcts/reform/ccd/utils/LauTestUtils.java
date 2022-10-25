package uk.gov.hmcts.reform.ccd.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.LauRecordHolder;

import java.util.Set;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class LauTestUtils {

    @Inject
    private ParameterResolver parameterResolver;

    @Inject
    private LauRecordHolder lauRecordHolder;

    public void verifyLauLogs(final Set<String> caseRefs) {
        if (parameterResolver.isLogAndAuditEnabled()) {
            assertThat(lauRecordHolder.getLauCaseRefList())
                    .containsExactlyInAnyOrderElementsOf(caseRefs);

            lauRecordHolder.getLauCaseRefList().clear();
        }
    }
}
