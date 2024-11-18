package uk.gov.hmcts.reform.ccd.service.remote;

import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.HearingDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.remote.clients.HearingClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.HearingDeletionRecordHolder;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@Service
@Slf4j
@RequiredArgsConstructor
public class DisposeHearingsRemoteOperation implements DisposeRemoteOperation {

    private final HearingDeletionRecordHolder hearingDeletionRecordHolder;
    private final HearingClient hearingClient;
    private final SecurityUtil securityUtil;
    private final ParameterResolver parameterResolver;

    @Override
    public void delete(final CaseData caseData) {
        if (caseData.getCaseType().equals(parameterResolver.getHearingCaseType())) {
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
