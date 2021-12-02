package uk.gov.hmcts.reform.ccd.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseEventRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkPrimaryKey;
import uk.gov.hmcts.reform.ccd.data.es.CaseDataElasticsearchOperations;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@Slf4j
public class CaseDeletionService {

    private final CaseDataRepository caseDataRepository;
    private final CaseEventRepository caseEventRepository;
    private final CaseLinkRepository caseLinkRepository;
    private final CaseDataElasticsearchOperations caseDataElasticsearchOperations;
    private final ParameterResolver parameterResolver;

    @Inject
    public CaseDeletionService(final CaseDataRepository caseDataRepository,
                               final CaseEventRepository caseEventRepository,
                               final CaseLinkRepository caseLinkRepository,
                               final CaseDataElasticsearchOperations caseDataElasticsearchOperations,
                               final ParameterResolver parameterResolver) {
        this.caseDataRepository = caseDataRepository;
        this.caseEventRepository = caseEventRepository;
        this.caseLinkRepository = caseLinkRepository;
        this.caseDataElasticsearchOperations = caseDataElasticsearchOperations;
        this.parameterResolver = parameterResolver;
    }

    @Transactional
    public void deleteCase(final CaseFamily caseFamily) {
        final CaseData caseData = caseFamily.getRootAncestor();
        log.info("About to delete case.reference:: {}", caseData.getReference());
        caseFamily.getFamilyMembers().forEach(item -> {
            deleteLinkedCase(item.getParentCase().getId(), item);
        });
        deleteCaseData(caseData);
        log.info("Deleted case.reference:: {}", caseData.getReference());
    }

    private void deleteLinkedCase(final Long parentCaseId, final CaseData caseData) {
        log.info("About to delete linked case.reference:: {}", caseData.getReference());
        final CaseLinkPrimaryKey caseLinkPrimaryKey = new CaseLinkPrimaryKey(parentCaseId, caseData.getId());
        caseLinkRepository.deleteById(caseLinkPrimaryKey);
        deleteCaseData(caseData);
        log.info("Deleted linked case.reference:: {}", caseData.getReference());
    }

    private void deleteCaseData(final CaseData caseData) {
        caseEventRepository.deleteByCaseDataId(caseData.getId());
        caseDataRepository.deleteById(caseData.getId());
        caseDataElasticsearchOperations.deleteByReference(getIndex(caseData.getCaseType()), caseData.getReference());
    }

    private String getIndex(final String caseType) {
        return String.format(parameterResolver.getCasesIndexNamePattern(), caseType).toLowerCase();
    }
}
