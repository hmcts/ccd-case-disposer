package uk.gov.hmcts.reform.ccd.util;

import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Named
@Getter
public class ProcessedCasesRecordHolder {

    private Set<Long> failedToDeleteCaseRefs = new HashSet<>();
    private Set<CaseData> processedCases = new HashSet<>();
    @Setter
    private Set<CaseData> simulatedCases = new HashSet<>();

    public void addFailedToDeleteCaseRef(final CaseData caseData) {
        failedToDeleteCaseRefs.add(caseData.getReference());
    }

    public void addProcessedCase(final CaseData caseData) {
        processedCases.add(caseData);
    }

    public boolean hasBeenProcessed(final Long caseRef) {
        return processedCases.stream()
            .anyMatch(caseData -> caseData.getReference().equals(caseRef));
    }

    public List<CaseData> getSuccessfullyDeletedCases() {
        return processedCases.stream()
            .filter(caseData -> !failedToDeleteCaseRefs.contains(caseData.getReference()))
            .collect(Collectors.toList());
    }

    public List<CaseData> getFailedToDeleteDeletedCases() {
        return processedCases.stream()
            .filter(caseData -> failedToDeleteCaseRefs.contains(caseData.getReference()))
            .collect(Collectors.toList());
    }
}
