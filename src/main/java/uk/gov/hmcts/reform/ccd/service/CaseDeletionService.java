package uk.gov.hmcts.reform.ccd.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.ccd.ApplicationParameters;
import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.es.CaseDataElasticsearchOperations;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@Slf4j
public class CaseDeletionService {

    private final CaseDataRepository caseDataRepository;
    private final CaseEventRepository caseEventRepository;
    private final CaseDataElasticsearchOperations caseDataElasticsearchOperations;
    private final ApplicationParameters parameters;

    @Inject
    public CaseDeletionService(final CaseDataRepository caseDataRepository,
                               final CaseEventRepository caseEventRepository,
                               final CaseDataElasticsearchOperations caseDataElasticsearchOperations,
                               final ApplicationParameters parameters) {
        this.caseDataRepository = caseDataRepository;
        this.caseEventRepository = caseEventRepository;
        this.caseDataElasticsearchOperations = caseDataElasticsearchOperations;
        this.parameters = parameters;
    }

    public List<CaseDataEntity> getExpiredCases() {
        return caseDataRepository.findExpiredCases(/*parameters.expirableTypes()*/);
    }

    @Transactional
    public void deleteCase(final CaseDataEntity caseData) {
        log.info("About to delete case.reference:: {}", caseData.getReference());
        caseEventRepository.deleteByCaseDataId(caseData.getId());
        caseDataRepository.deleteById(caseData.getId());
        caseDataElasticsearchOperations.deleteByReference(getIndex(caseData.getCaseType()), caseData.getReference());
        log.info("Deleted case.reference:: {}", caseData.getReference());
    }

    private String getIndex(final String caseType) {
        return String.format(parameters.getCasesIndexNamePattern(), caseType);
    }
}
