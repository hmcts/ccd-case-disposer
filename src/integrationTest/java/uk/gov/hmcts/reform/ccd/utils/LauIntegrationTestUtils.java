package uk.gov.hmcts.reform.ccd.utils;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.util.log.LauRecordHolder;

import java.util.List;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class LauIntegrationTestUtils {

    @Inject
    private LauRecordHolder lauRecordHolder;

    public void verifyLauLogs(final List<Long> caseRefs) {
        assertThat(lauRecordHolder.getLauCaseRefList())
                .containsExactlyInAnyOrderElementsOf(Lists.transform(caseRefs, Functions.toStringFunction()));

        lauRecordHolder.getLauCaseRefList().clear();
    }
}
