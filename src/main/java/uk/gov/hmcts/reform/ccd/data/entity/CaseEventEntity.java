package uk.gov.hmcts.reform.ccd.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "case_event")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CaseEventEntity {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "event_id")
    private String eventId;
    @Column(name = "event_name")
    private String eventName;
    @Column(name = "case_data_id")
    private Long caseDataId;
}
