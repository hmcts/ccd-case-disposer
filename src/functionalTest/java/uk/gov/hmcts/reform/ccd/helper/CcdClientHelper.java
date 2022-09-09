package uk.gov.hmcts.reform.ccd.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;

import static uk.gov.hmcts.reform.ccd.constants.TestConstants.CREATE_CASE_EVENT;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.JURISDICTION;

@Component
public class CcdClientHelper {

    @Inject
    private SecurityUtils securityUtils;

    @Inject
    private CoreCaseDataApi coreCaseDataApi;

    public List<CaseDetails> getCcdCases(final List<String> caseRefs) {
        final List<CaseDetails> caseDetailsList = new ArrayList<>();
        caseRefs.forEach(caseRef -> {
            try {
                caseDetailsList.add(coreCaseDataApi.getCase(securityUtils.getIdamClientToken(),
                        securityUtils.getServiceAuthorization(),
                        caseRef));
            } catch (final Exception exception) {
                exception.printStackTrace();
            }

        });
        return caseDetailsList;
    }

    public List<CaseDetails> getCasesByCaseType(final String caseType) {
        return coreCaseDataApi.searchForCaseworker(securityUtils.getIdamClientToken(),
                securityUtils.getServiceAuthorization(),
                securityUtils.getUserDetails().getId(),
                JURISDICTION,
                caseType,
                new HashMap<>());
    }

    public StartEventResponse getStartEventResponse(final String caseType) {
        return coreCaseDataApi.startForCaseworker(
                securityUtils.getIdamClientToken(),
                securityUtils.getServiceAuthorization(),
                securityUtils.getUserDetails().getId(),
                JURISDICTION,
                caseType,
                CREATE_CASE_EVENT
        );
    }

    public StartEventResponse getStartEventResponse(final CaseDetails caseDetails,
                                                    final String caseType,
                                                    final String eventId) {
        return coreCaseDataApi.startEventForCaseWorker(
                securityUtils.getIdamClientToken(),
                securityUtils.getServiceAuthorization(),
                securityUtils.getUserDetails().getId(),
                JURISDICTION,
                caseType,
                Long.toString(caseDetails.getId()),
                eventId
        );
    }

    public CaseDetails submitCase(final CaseDataContent caseDataContent, final String caseType) {
        return coreCaseDataApi.submitForCaseworker(
                securityUtils.getIdamClientToken(),
                securityUtils.getServiceAuthorization(),
                securityUtils.getUserDetails().getId(),
                JURISDICTION,
                caseType,
                true,
                caseDataContent
        );
    }

    public void submitEvent(final CaseDetails caseDetails,
                            final String caseType,
                            final CaseDataContent caseDataContent) {
        coreCaseDataApi.submitEventForCaseWorker(securityUtils.getIdamClientToken(),
                securityUtils.getServiceAuthorization(),
                securityUtils.getUserDetails().getId(),
                JURISDICTION,
                caseType,
                Long.toString(caseDetails.getId()),
                true,
                caseDataContent);
    }
}