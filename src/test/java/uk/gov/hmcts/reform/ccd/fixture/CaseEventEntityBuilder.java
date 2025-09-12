package uk.gov.hmcts.reform.ccd.fixture;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.ccd.data.entity.CaseEventEntity;

@AllArgsConstructor
public class CaseEventEntityBuilder {
    private final Long id;
    private final String eventId;
    private final String eventName;
    private final Long caseDataId;

    public CaseEventEntity build() {
        CaseEventEntity caseEventEntity = new CaseEventEntity();
        caseEventEntity.setId(id);
        caseEventEntity.setCaseDataId(caseDataId);
        caseEventEntity.setEventName(eventName);
        caseEventEntity.setEventId(eventId);

        return caseEventEntity;
    }
}
