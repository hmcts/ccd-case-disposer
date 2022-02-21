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

import java.util.List;
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
    public void deleteCases(final List<CaseFamily> linkedFamilies) {
        linkedFamilies.forEach(caseFamily -> deleteLinkedCases(caseFamily.getLinkedCases()));
        linkedFamilies.forEach(this::deleteCase);
    }

    void deleteCase(final CaseFamily caseFamily) {
        try {
            final CaseData rootCaseData = caseFamily.getRootCase();
            final List<CaseData> linkedCases = caseFamily.getLinkedCases();
            log.info("About to delete case.reference:: {}", rootCaseData.getReference());
            linkedCases.forEach(this::deleteCaseData);
            deleteCaseData(rootCaseData);
            log.info("Deleted case.reference:: {}", rootCaseData.getReference());
        } catch (Exception e) { // Catch all exception
            log.error("some error");
        }
    }

    void deleteLinkedCases(final List<CaseData> linkedCases) {
        linkedCases.forEach(item -> deleteLinkedCase(item.getParentCase().getId(), item));
    }

    private void deleteLinkedCase(final Long parentCaseId, final CaseData caseData) {
        try {
            log.info("About to delete linked case.reference:: {}", caseData.getReference());
            final CaseLinkPrimaryKey caseLinkPrimaryKey = new CaseLinkPrimaryKey(parentCaseId, caseData.getId());
            caseLinkRepository.findById(caseLinkPrimaryKey)
                .ifPresent(caseLinkRepository::delete);
            log.info("Deleted linked case.reference:: {}", caseData.getReference());
        } catch (Exception e) { // Catch all exception
            log.error("Could not delete linked case.reference:: {}", caseData.getReference());
        }
    }

    private void deleteCaseData(final CaseData caseData) {
        caseEventRepository.deleteByCaseDataId(caseData.getId());
        caseDataRepository.findById(caseData.getId())
            .ifPresent(caseDataRepository::delete);
        caseDataElasticsearchOperations.deleteByReference(getIndex(caseData.getCaseType()), caseData.getReference());
    }

    private String getIndex(final String caseType) {
        return String.format(parameterResolver.getCasesIndexNamePattern(), caseType).toLowerCase();
    }
}
