package uk.gov.hmcts.reform.ccd.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.Objects;

@Value
@Builder
@AllArgsConstructor
public class CaseData {
    Long id;
    Long reference;
    String caseType;
    String jurisdiction;
    LocalDate resolvedTtl;
    Long familyId;
    CaseData parentCase;

    @Override
    public boolean equals(Object other) {
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        CaseData caseData = (CaseData) other;
        return Objects.equals(id, caseData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
