package uk.gov.hmcts.reform.ccd.utils;


import jakarta.inject.Inject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.util.log.HearingDeletionRecordHolder;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.HEARINGS_DELETE;

@Component
public class HearingRemoteDeletionVerifier implements RemoteDeletionVerifier {

    @Inject
    private HearingDeletionRecordHolder hearingDeletionRecordHolder;

    public void verifyRemoteDeletion(final List<Long> caseRefDeletedHearings) {
        caseRefDeletedHearings.forEach(caseRef -> {
            final Integer caseHearingsDeletionMocks = HEARINGS_DELETE.get(Long.toString(caseRef));
            // This if statement guards against non hearing cases. Most of the caseRefs will be of type
            // HearingRecordings which means that the document endpoint will be called; instead, the hearing endpoint.
            if (caseHearingsDeletionMocks != null) {
                final int caseHearingsDeletionActualResults = hearingDeletionRecordHolder
                    .getHearingDeletionResults(Long.toString(caseRef));

                assertThat(caseHearingsDeletionActualResults).isEqualTo(caseHearingsDeletionMocks);
            }
        });
    }
}
