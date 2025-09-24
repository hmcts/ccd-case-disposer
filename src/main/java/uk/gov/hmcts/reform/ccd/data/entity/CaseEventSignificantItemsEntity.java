package uk.gov.hmcts.reform.ccd.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "case_event_significant_items")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CaseEventSignificantItemsEntity {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "case_event_id")
    private Long caseEventId;
}
