package uk.gov.hmcts.reform.ccd.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import javax.inject.Inject;

import static uk.gov.hmcts.reform.ccd.client.model.CaseDataContent.builder;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.CASE_DATA_TTL;
import static uk.gov.hmcts.reform.ccd.helper.ObjectMapperBuilder.caseData;

@Component
public class DataStoreEventCreator {

    @Inject
    private CcdClientHelper ccdClientHelper;

    public void createDataStoreEvent(final CaseDetails caseDetails,
                                     final String caseType,
                                     final String eventId) {
        final StartEventResponse startEventResponse = ccdClientHelper.getStartEventResponse(caseDetails, caseType,
                eventId);

        final CaseDataContent caseDataContent = createCaseDataContent(caseDetails, startEventResponse, eventId);

        ccdClientHelper.submitEvent(caseDetails, caseType, caseDataContent);
    }

    private CaseDataContent createCaseDataContent(final CaseDetails caseDetails,
                                                  final StartEventResponse startEventResponse,
                                                  final String eventId) {
        return builder()
                .data(caseData(CASE_DATA_TTL))
                .caseReference(caseDetails.getId().toString())
                .eventToken(startEventResponse.getToken())
                .event(Event
                        .builder()
                        .id(eventId)
                        .summary("Case updated")
                        .description("Functional Test Case CCD Case Disposer")
                        .build()
                )
                .build();
    }
}