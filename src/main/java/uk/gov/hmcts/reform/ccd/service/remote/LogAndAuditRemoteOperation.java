package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.lau.ActionLog;
import uk.gov.hmcts.reform.ccd.data.lau.CaseActionPostRequestResponse;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.LogAndAuditException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.util.log.LauRecordHolder;

import java.text.SimpleDateFormat;

import static java.sql.Timestamp.valueOf;
import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.LAU_SAVE_PATH;

@Service
@Slf4j
@Qualifier("LogAndAuditRemoteOperation")
public class LogAndAuditRemoteOperation {

    private final ParameterResolver parameterResolver;

    private final RestClientBuilder restClientBuilder;

    private final SecurityUtil securityUtil;

    private final LauRecordHolder lauRecordHolder;

    private final Gson gson = new Gson();


    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public LogAndAuditRemoteOperation(final ParameterResolver parameterResolver,
                                      final RestClientBuilder restClientBuilder,
                                      final LauRecordHolder lauRecordHolder,
                                      final SecurityUtil securityUtil) {
        this.parameterResolver = parameterResolver;
        this.restClientBuilder = restClientBuilder;
        this.lauRecordHolder = lauRecordHolder;
        this.securityUtil = securityUtil;
    }

    public void postCaseDeletionToLogAndAudit(final CaseData caseData) {
        if (parameterResolver.isLogAndAuditEnabled()) {
            try {
                final CaseActionPostRequestResponse caseActionPostRequestResponse =
                        buildCaseActionPostRequest(caseData);
                final String logAndAuditPostResponse =
                        restClientBuilder.postRequestWithServiceAuthHeader(parameterResolver.getLogAndAuditHost(),
                                LAU_SAVE_PATH,
                                gson.toJson(caseActionPostRequestResponse));

                logResponse(logAndAuditPostResponse);
            } catch (final Exception exception) {
                final String errorMessage = String.format("Error posting to Log and Audit for case : %s",
                        caseData.getReference());
                log.error(errorMessage, exception);
                throw new LogAndAuditException(errorMessage, exception);
            }
        }
    }

    private void logResponse(final String logAndAuditPostResponse) {
        try {
            final CaseActionPostRequestResponse caseActionResults =
                    gson.fromJson(logAndAuditPostResponse, CaseActionPostRequestResponse.class);

            logLauRecord(caseActionResults);

            log.info("Case data with case ref: {} successfully posted to Log and Audit",
                    caseActionResults.getActionLog().getCaseRef());

        } catch (final JsonParseException jsonParseException) {
            final String errorMessage = "Unable to map json to object Log and Audit endpoint response due"
                    + " to following endpoint response: ".concat(logAndAuditPostResponse);
            log.error(errorMessage);
            throw new LogAndAuditException(errorMessage);
        }

    }

    private void logLauRecord(final CaseActionPostRequestResponse caseActionResults) {
        lauRecordHolder.addLauCaseRef(caseActionResults.getActionLog().getCaseRef());
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