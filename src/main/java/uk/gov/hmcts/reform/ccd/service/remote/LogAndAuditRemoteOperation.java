package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.JsonParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.lau.ActionLog;
import uk.gov.hmcts.reform.ccd.data.lau.CaseActionPostRequestResponse;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.LogAndAuditException;
import uk.gov.hmcts.reform.ccd.service.remote.clients.LauClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.LauRecordHolder;

import java.rmi.Remote;
import java.text.SimpleDateFormat;

import static java.sql.Timestamp.valueOf;
import static java.time.LocalDateTime.now;

@Service
@Slf4j
@Qualifier("LogAndAuditRemoteOperation")
@RequiredArgsConstructor
public class LogAndAuditRemoteOperation {

    private final LauClient lauClient;
    private final SecurityUtil securityUtil;
    private final LauRecordHolder lauRecordHolder;


    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";


    public void postCaseDeletionToLogAndAudit(final CaseData caseData) {
        try {
            final CaseActionPostRequestResponse caseActionPostRequestResponse =
                buildCaseActionPostRequest(caseData);
            final ResponseEntity<CaseActionPostRequestResponse> logAndAuditPostResponse = lauClient.postLauAudit(
                securityUtil.getServiceAuthorization(),
                caseActionPostRequestResponse
            );

            logResponse(logAndAuditPostResponse.getBody());

            if (!logAndAuditPostResponse.getStatusCode().is2xxSuccessful()) {
                final String errorMessage = String
                    .format("Unexpected response code %d while sending data to Log and Audit for case: %s",
                            logAndAuditPostResponse.getStatusCode().value(), caseData.getReference());

                log.error(errorMessage);

                throw new LogAndAuditException(errorMessage);
            }


        } catch (final Exception exception) {
            final String errorMessage = String.format(
                "Error posting to Log and Audit for case : %s",
                caseData.getReference()
            );
            log.error(errorMessage, exception);
            throw new LogAndAuditException(errorMessage, exception);
        }
    }

    private void logResponse(final CaseActionPostRequestResponse logAndAuditPostResponse) {
        try {
            lauRecordHolder.addLauCaseRef(logAndAuditPostResponse.getActionLog().getCaseRef());

            log.info(
                "Case data with case ref: {} successfully posted to Log and Audit",
                logAndAuditPostResponse.getActionLog().getCaseRef()
            );

        } catch (final JsonParseException jsonParseException) {
            final String errorMessage = "Unable to map json to object Log and Audit endpoint response due"
                + " to following endpoint response: ".concat(logAndAuditPostResponse.toString());
            log.error(errorMessage);
            throw new LogAndAuditException(errorMessage);
        }

    }

    private CaseActionPostRequestResponse buildCaseActionPostRequest(final CaseData caseData) {
        return new CaseActionPostRequestResponse(ActionLog.builder()
                                                     .userId(securityUtil.getUserDetails().getId())
                                                     .caseAction("DELETE")
                                                     .caseTypeId(caseData.getCaseType())
                                                     .caseRef(caseData.getReference().toString())
                                                     .caseJurisdictionId(caseData.getJurisdiction())
                                                     .timestamp(getTimestamp())
                                                     .build());
    }


    private String getTimestamp() {
        return new SimpleDateFormat(TIMESTAMP_PATTERN).format(valueOf(now()));
    }
}
