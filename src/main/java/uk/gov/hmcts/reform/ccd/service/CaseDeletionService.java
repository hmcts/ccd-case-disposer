package uk.gov.hmcts.reform.ccd.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.ccd.ApplicationParameters;
import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.es.CaseDataElasticsearchOperations;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@Slf4j
public class CaseDeletionService {

    private final CaseDataRepository caseDataRepository;
    private final CaseEventRepository caseEventRepository;
    private final CaseLinkRepository caseLinkRepository;
    private final CaseDataElasticsearchOperations caseDataElasticsearchOperations;
    private final ApplicationParameters parameters;

    @Inject
    public CaseDeletionService(final CaseDataRepository caseDataRepository,
                               final CaseEventRepository caseEventRepository,
                               final CaseLinkRepository caseLinkRepository,
                               final CaseDataElasticsearchOperations caseDataElasticsearchOperations,
                               final ApplicationParameters parameters) {
        this.caseDataRepository = caseDataRepository;
        this.caseEventRepository = caseEventRepository;
        this.caseLinkRepository = caseLinkRepository;
        this.caseDataElasticsearchOperations = caseDataElasticsearchOperations;
        this.parameters = parameters;
    }

    public List<CaseDataEntity> getExpiredCases() {
        return caseDataRepository.findExpiredCases(parameters.getDeletableCaseTypes());
    }

    public Map<CaseDataEntity, List<CaseLinkEntity>> getLinkedCases(final CaseDataEntity caseData) {
        final List<CaseLinkEntity> linkEntities = caseLinkRepository.findByCaseId(caseData.getId());

        return Map.of(caseData, linkEntities);
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
