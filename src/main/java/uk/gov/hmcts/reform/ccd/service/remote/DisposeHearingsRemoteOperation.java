package uk.gov.hmcts.reform.ccd.service.remote;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.HearingDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.HearingDeletionRecordHolder;

import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_HEARINGS_PATH;

@Service
@Slf4j
public class DisposeHearingsRemoteOperation implements DisposeRemoteOperation {

    private final ParameterResolver parameterResolver;
    private final HearingDeletionRecordHolder hearingDeletionRecordHolder;
    private final CcdRestClientBuilder ccdRestClientBuilder;

    @Autowired
    public DisposeHearingsRemoteOperation(final CcdRestClientBuilder ccdRestClientBuilder,
                                          final ParameterResolver parameterResolver,
                                          final HearingDeletionRecordHolder hearingDeletionRecordHolder) {
        this.ccdRestClientBuilder = ccdRestClientBuilder;
        this.parameterResolver = parameterResolver;
        this.hearingDeletionRecordHolder = hearingDeletionRecordHolder;
    }

    @Override
    public void delete(final CaseData caseData) {
        final String caseRef = caseData.getReference().toString();
        try {
            final Response hearingDeleteResponse = deleteHearings(caseRef);
            logHearingDisposal(caseRef, hearingDeleteResponse.getStatus());
        } catch (final Exception ex) {
            final String errorMessage = String.format("Error deleting hearing for case : %s", caseRef);
            log.error(errorMessage, ex);
            throw new HearingDeletionException(errorMessage, ex);
        }
    }

    private void logHearingDisposal(final String caseRef, final int status) {
        hearingDeletionRecordHolder.setHearingDeletionResults(caseRef, status);
    }

    @Async
    Response deleteHearings(final String caseId) {
        return ccdRestClientBuilder
            .deleteRequestWithAuthHeaders(parameterResolver.getHearingHost(), DELETE_HEARINGS_PATH, caseId);
    }
}
