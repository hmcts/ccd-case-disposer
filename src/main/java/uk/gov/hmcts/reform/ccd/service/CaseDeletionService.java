package uk.gov.hmcts.reform.ccd.service;


import jakarta.inject.Named;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionException;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;

@Named
@Slf4j
@RequiredArgsConstructor
public class CaseDeletionService {
    private final CaseDeletionSteps steps;
    private final ProcessedCasesRecordHolder processedCasesRecordHolder;

    @Transactional
    public void deleteCaseData(@NonNull final CaseData caseData)  {
        boolean deletedCaseLinks = false;
        try {
            deletedCaseLinks = steps.deleteCaseLinks(caseData);
        } catch (DataIntegrityViolationException | PersistenceException | TransactionException ex) {
            log.error("CaseDeletionService:Constraint/persistence error deleting case:: {} ({}) {}",
                      caseData.getReference(), caseData.getJurisdiction(), ex.getMessage(), ex
            );
        }
        try {
            if (deletedCaseLinks) {
                steps.deleteCase(caseData);
            }
        }  catch (DataIntegrityViolationException | PersistenceException ex) {
            log.error("CaseDeletionService:Constraint/persistence error deleting case:: {} ({}) {}",
                      caseData.getReference(), caseData.getJurisdiction(), ex.getMessage(), ex
            );
        }
    }

}
