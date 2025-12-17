package uk.gov.hmcts.reform.ccd.utils;


import jakarta.inject.Inject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.util.log.HearingDeletionRecordHolder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.HEARINGS_DELETE;

@Component
public class HearingRemoteDeletionVerifier implements RemoteDeletionVerifier<Map<String, Integer>> {

    @Inject
    private HearingDeletionRecordHolder hearingDeletionRecordHolder;

    @Override
    public Map<String, Integer> snapshot() {
        return hearingDeletionRecordHolder.snapshot();
    }

    @Override
    public void clear() {
        hearingDeletionRecordHolder.clear();
    }

    public void assertDeletionResults(Map<String, Integer> snapshot,
        List<Long> caseRefDeletedHearings) {

        caseRefDeletedHearings.forEach(caseRef -> {
            String caseRefStr = caseRef.toString();
            Integer expected = HEARINGS_DELETE.get(caseRefStr);
            // This if statement guards against non hearing cases. Most of the caseRefs will be of type
            // HearingRecordings which means that the document endpoint will be called; instead, the hearing endpoint.
            if (expected != null) {
                Integer actual = snapshot.get(caseRefStr);
                assertThat(actual)
                    .as("Hearing deletion result missing for caseRef %s", caseRefStr)
                    .isNotNull();

                assertThat(actual)
                    .as("Hearing deletion status mismatch for caseRef %s", caseRefStr)
                    .isEqualTo(expected);
            }
        });
    }
}
