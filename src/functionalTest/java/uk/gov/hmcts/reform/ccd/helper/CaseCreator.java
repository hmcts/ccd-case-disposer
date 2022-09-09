package uk.gov.hmcts.reform.ccd.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.util.log.DataStoreRecordHolder;

import javax.inject.Inject;

import static uk.gov.hmcts.reform.ccd.client.model.CaseDataContent.builder;
import static uk.gov.hmcts.reform.ccd.client.model.Classification.PUBLIC;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.CASE_DATA_TTL;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.CREATE_CASE_EVENT;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.UPDATE_CASE_EVENT;
import static uk.gov.hmcts.reform.ccd.helper.ObjectMapperBuilder.caseData;

@Component
public class CaseCreator {

    @Inject
    private DataStoreEventCreator dataStoreEventCreator;

    @Inject
    private DataStoreRecordHolder dataStoreRecordHolder;

    @Inject
    private CcdClientHelper ccdClientHelper;

    public void createCase(final String caseType) {
        final StartEventResponse startEventResponse = ccdClientHelper.getStartEventResponse(caseType);

        final CaseDataContent caseDataContent = createCaseDataContent(startEventResponse);

        final CaseDetails caseDetails = ccdClientHelper.submitCase(caseDataContent, caseType);

        dataStoreEventCreator.createDataStoreEvent(caseDetails, caseType, UPDATE_CASE_EVENT);

        dataStoreRecordHolder.addRecord(caseDetails.getCaseTypeId(), Long.toString(caseDetails.getId()));
    }

    private CaseDataContent createCaseDataContent(final StartEventResponse eventResponse) {
        return builder()
                .data(caseData(CASE_DATA_TTL))
                .securityClassification(PUBLIC)
                .eventToken(eventResponse.getToken())
                .event(Event
                        .builder()
                        .id(CREATE_CASE_EVENT)
                        .summary("Case created")
                        .description("Functional Test Case CCD Case Disposer")
                        .build())
                .build();
    }
}