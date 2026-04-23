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
import uk.gov.hmcts.reform.ccd.util.perf.LogExecutionTime;

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

    @LogExecutionTime("Log and Audit")
    public void postCaseDeletionToLogAndAudit(final CaseData caseData) {
        final CaseActionPostRequestResponse caseActionPostRequestResponse =
            buildCaseActionPostRequest(caseData);

        final String caseRef = caseData.getReference().toString();
        final ResponseEntity<CaseActionPostRequestResponse> logAndAuditPostResponse;
        try {
            logAndAuditPostResponse = lauClient.postLauAudit(
                securityUtil.getServiceAuthorization(),
                caseActionPostRequestResponse
            );
        } catch (final Exception ex) {
            log.error("Error posting to Log and Audit for case : {}", caseRef, ex);
            throw new LogAndAuditException(caseRef, ex);
        }

        logResponse(logAndAuditPostResponse.getBody());

        if (!logAndAuditPostResponse.getStatusCode().is2xxSuccessful()) {
            int statusCode = logAndAuditPostResponse.getStatusCode().value();
            log.error(
                "Unexpected response code {} while sending data to Log and Audit for case: {}",
                statusCode,
                caseRef
            );

            throw new LogAndAuditException(statusCode, caseRef);
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
