package uk.gov.hmcts.reform.ccd.service;

import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;

import java.util.List;
import javax.inject.Inject;

public class CaseDeletionService {

    private final CaseDataRepository caseDataRepository;
    private final CaseEventRepository caseEventRepository;

    @Inject
    public CaseDeletionService(CaseDataRepository caseDataRepository, CaseEventRepository caseEventRepository) {
        this.caseDataRepository = caseDataRepository;
        this.caseEventRepository = caseEventRepository;
    }

    public void deleteExpiredCases() {
        final List<CaseDataEntity> expiredCases = caseDataRepository.findExpiredCases();
        expiredCases.forEach(caseDataEntity -> {
            caseEventRepository.deleteByCaseDataId(caseDataEntity.getId());
            caseDataRepository.deleteById(caseDataEntity.getId());
        });

    }
}
