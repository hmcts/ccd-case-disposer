package uk.gov.hmcts.reform.ccd.service.remote;

import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.HearingDeletionException;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.HearingDeletionRecordHolder;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.HEARING_RECORDINGS_CASE_TYPE;

@Service
@Slf4j
@RequiredArgsConstructor
public class DisposeHearingsRemoteOperation implements DisposeRemoteOperation {

    private final HearingDeletionRecordHolder hearingDeletionRecordHolder;
    private final HearingClient hearingClient;
    private final SecurityUtil securityUtil;

    @Override
    public void delete(final CaseData caseData) {
        if (caseData.getCaseType().equals(HEARING_RECORDINGS_CASE_TYPE)) {
            final List<String> caseRef = List.of(String.valueOf(caseData.getReference()));
            try {
                final Response deleteHearingsResponse = deleteHearings(caseRef);

                logHearingDisposal(caseRef.getFirst(), deleteHearingsResponse.status());

                if (deleteHearingsResponse.status() != NO_CONTENT.value()) {
                    final String errorMessage = String
                            .format("Unexpected response code %d while deleting hearing for case: %s",
                            deleteHearingsResponse.status(), caseRef);

                    throw new HearingDeletionException(errorMessage);
                }
            } catch (final Exception ex) {
                final String errorMessage = String.format("Error deleting hearing for case : %s", caseRef);
                log.error(errorMessage, ex);
                throw new HearingDeletionException(errorMessage, ex);
            }
        }
    }

    private void logHearingDisposal(final String caseRef, final int status) {
        hearingDeletionRecordHolder.setHearingDeletionResults(caseRef, status);
    }


    private Response deleteHearings(final List<String> caseRefs) {
        return hearingClient.deleteHearing(securityUtil.getIdamClientToken(),
                securityUtil.getServiceAuthorization(),
                caseRefs);
    }
}
