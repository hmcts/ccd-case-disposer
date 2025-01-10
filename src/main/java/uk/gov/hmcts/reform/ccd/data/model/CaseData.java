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
    public String toString() {
        return "CaseData{id=" + id
            + ", reference=" + reference
            + ", caseType='" + caseType + "'"
            + ", resolvedTtl=" + resolvedTtl
            + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CaseData caseData = (CaseData) o;
        return Objects.equals(id, caseData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
