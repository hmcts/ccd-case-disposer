package uk.gov.hmcts.reform.ccd.utils;

import jakarta.inject.Inject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.util.log.LauRecordHolder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class LauRemoteDeletionVerifier implements RemoteDeletionVerifier<Set<String>> {

    @Inject
    private LauRecordHolder lauRecordHolder;

    @Override
    public Set<String> snapshot() {
        return lauRecordHolder.snapshot();
    }

    @Override
    public void clear() {
        lauRecordHolder.clear();
    }

    public void assertDeletionResults(Set<String> snapshot,
        List<Long> caseRefs) {

        List<String> expected = caseRefs.stream()
            .map(String::valueOf).toList();

        assertThat(snapshot).containsExactlyInAnyOrderElementsOf(expected);
    }
}
