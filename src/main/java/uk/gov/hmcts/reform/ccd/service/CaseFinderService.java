package uk.gov.hmcts.reform.ccd.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.ApplicationParameters;
import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@Slf4j
public class CaseFinderService {
    private final CaseDataRepository caseDataRepository;
    private final CaseLinkRepository caseLinkRepository;
    private final ApplicationParameters parameters;

    @Inject
    public CaseFinderService(final CaseDataRepository caseDataRepository,
                             final CaseLinkRepository caseLinkRepository,
                             final ApplicationParameters parameters) {
        this.caseDataRepository = caseDataRepository;
        this.caseLinkRepository = caseLinkRepository;
        this.parameters = parameters;
    }

    List<CaseDataEntity> getExpiredCases() {
        return caseDataRepository.findExpiredCases(parameters.getDeletableCaseTypes());
    }

    List<CaseDataEntity> getLinkedCases(final CaseDataEntity caseData) {
        final List<CaseLinkEntity> linkEntities = caseLinkRepository.findByCaseId(caseData.getId());

        final List<Long> linkedCaseIds = linkEntities.stream()
            .map(CaseLinkEntity::getCaseId)
            .collect(Collectors.toUnmodifiableList());

        return caseDataRepository.findAllById(linkedCaseIds);
    }

    Boolean isCaseDueDeletion(@NonNull final CaseDataEntity caseData) {
        return isDeletableCaseType(caseData.getCaseType()) && isExpired(caseData.getResolvedTtl());
    }

    Boolean isAllDueDeletion(final List<CaseDataEntity> linkedCases) {
        return linkedCases.stream()
            .allMatch(this::isCaseDueDeletion);
    }

    public List<CaseDataEntity> findDeletableCandidates() {
        final List<CaseDataEntity> expiredCases = getExpiredCases();

        return expiredCases.stream()
            .flatMap(caseData -> {
                final List<CaseDataEntity> linkedCases = getLinkedCases(caseData);
                return isAllDueDeletion(linkedCases)
                    ? Stream.concat(Stream.of(caseData), linkedCases.stream())
                    : logNonQualifyingCase(caseData, linkedCases);
            })
            .collect(Collectors.toUnmodifiableList());
    }

    private Boolean isExpired(@NonNull final LocalDate caseTtl) {
        final LocalDate today = LocalDate.now();
        return !caseTtl.isAfter(today);
    }

    private Boolean isDeletableCaseType(@NonNull final String caseType) {
        return parameters.getDeletableCaseTypes().contains(caseType);
    }

    private Stream<CaseDataEntity> logNonQualifyingCase(final CaseDataEntity caseData,
                                                        final List<CaseDataEntity> linkedCases) {
        final String message = "Not deleting case.reference {}:: "
            + "one or more of the following linked cases.references {} do not meet the criteria for case deletion.";
        final List<Long> linkedCaseIds = linkedCases.stream()
            .map(CaseDataEntity::getReference)
            .collect(Collectors.toUnmodifiableList());
        log.info(message, caseData.getReference(), linkedCaseIds);

        return Stream.empty();
    }

}
