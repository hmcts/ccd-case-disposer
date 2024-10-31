package uk.gov.hmcts.reform.ccd.utils;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.util.log.LauRecordHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class LauRemoteDeletionVerifier implements RemoteDeletionVerifier {

    @Inject
    private LauRecordHolder lauRecordHolder;

    public void verifyRemoteDeletion(final List<Long> caseRefs) {
        assertThat(lauRecordHolder.getLauCaseRefList())
            .containsExactlyInAnyOrderElementsOf(Lists.transform(caseRefs, Functions.toStringFunction()));

        lauRecordHolder.getLauCaseRefList().clear();
    }
}
