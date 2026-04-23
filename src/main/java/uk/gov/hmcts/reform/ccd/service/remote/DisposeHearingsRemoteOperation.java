package uk.gov.hmcts.reform.ccd.service.remote;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
            final String caseRef = caseData.getReference().toString();
            final ResponseEntity<Void> deleteHearingsResponse;
            try {
                deleteHearingsResponse = deleteHearings(List.of(caseRef));
            } catch (Exception ex) {
                log.error("Error deleting hearing for case : {}", caseRef, ex);
                throw new HearingDeletionException(caseRef, ex);
            }

            final int statusCode = deleteHearingsResponse.getStatusCode().value();

            logHearingDisposal(caseRef, statusCode);

            if (statusCode != NO_CONTENT.value()) {
                throw new HearingDeletionException(statusCode, caseRef);
            }
        }
    }

    private void logHearingDisposal(final String caseRef, final int status) {
        hearingDeletionRecordHolder.setHearingDeletionResults(caseRef, status);
    }

    private ResponseEntity<Void> deleteHearings(final List<String> caseRefs) {
        return hearingClient.deleteHearing(securityUtil.getIdamClientToken(),
                securityUtil.getServiceAuthorization(),
                caseRefs);
    }
}
