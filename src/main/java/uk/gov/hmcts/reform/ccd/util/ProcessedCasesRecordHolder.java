package uk.gov.hmcts.reform.ccd.util;

import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Named
@Getter
public class ProcessedCasesRecordHolder {

    private final Set<Long> failedToDeleteCaseRefs = new HashSet<>();
    private final Set<CaseData> processedCases = new HashSet<>();

    @Setter
    private Set<CaseData> simulatedCases = new HashSet<>();

    public void addFailedToDeleteCaseRef(final CaseData caseData) {
        failedToDeleteCaseRefs.add(caseData.getReference());
    }

    public void addProcessedCase(final CaseData caseData) {
        processedCases.add(caseData);
    }

    public List<CaseData> getSuccessfullyDeletedCases() {
        return processedCases.stream()
            .filter(caseData -> !failedToDeleteCaseRefs.contains(caseData.getReference()))
            .toList();
    }

    public List<CaseData> getFailedToDeleteDeletedCases() {
        return processedCases.stream()
            .filter(caseData -> failedToDeleteCaseRefs.contains(caseData.getReference()))
            .toList();
    }

    // Clearing state for integration tests
    // This could be done by using @DirtiesContext annotation
    // on the tests, but it slows tests down significantly
    public void clearState() {
        failedToDeleteCaseRefs.clear();
        processedCases.clear();
        simulatedCases.clear();
    }
}
