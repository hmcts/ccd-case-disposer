package uk.gov.hmcts.reform.ccd.data.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "case_event")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CaseEventEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "event_id")
    private String eventId;
    @Column(name = "event_name")
    private String eventName;
    @Column(name = "case_data_id")
    private Long caseDataId;
}
