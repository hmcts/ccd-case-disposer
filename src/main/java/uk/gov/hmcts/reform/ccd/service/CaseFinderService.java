package uk.gov.hmcts.reform.ccd.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.data.dao.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.dao.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.RetentionStatus;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;

import static java.util.Collections.emptyList;

@Named
@Slf4j
public class CaseFinderService {
    private final CaseDataRepository caseDataRepository;
    private final CaseLinkRepository caseLinkRepository;
    private final ParameterResolver parameterResolver;

    @Inject
    public CaseFinderService(final CaseDataRepository caseDataRepository,
                             final CaseLinkRepository caseLinkRepository,
                             final ParameterResolver parameterResolver) {
        this.caseDataRepository = caseDataRepository;
        this.caseLinkRepository = caseLinkRepository;
        this.parameterResolver = parameterResolver;
    }

    List<CaseDataEntity> getExpiredCases() {
        return caseDataRepository.findExpiredCases(parameterResolver.getDeletableCaseTypes());
    }

    List<CaseDataEntity> getLinkedCases(final CaseDataEntity caseData) {
        final List<CaseLinkEntity> linkEntities = caseLinkRepository.findByCaseId(caseData.getId());

        final List<Long> linkedCaseIds = linkEntities.stream()
            .map(CaseLinkEntity::getLinkedCaseId)
            .collect(Collectors.toUnmodifiableList());

        return caseDataRepository.findAllById(linkedCaseIds);
    }

    Boolean isCaseDueDeletion(@NonNull final CaseDataEntity caseData) {
        return isDeletableCaseType(caseData.getCaseType()) && isExpired(caseData.getResolvedTtl());
    }

    Boolean isAllDueDeletion(final CaseDataEntity parentCase, final List<CaseDataEntity> linkedCases) {
        final List<CaseDataEntity> caseDataEntities = Stream.concat(Stream.of(parentCase), linkedCases.stream())
            .collect(Collectors.toUnmodifiableList());

        return caseDataEntities.stream()
            .allMatch(this::isCaseDueDeletion);
    }

    public List<CaseData> findCasesDueDeletion() {
        final List<CaseDataEntity> candidateCases = findCandidateCases();

        final Map<RetentionStatus, List<CaseData>> partitioned = findLinkedCasesAndPartition(candidateCases);

        final List<Long> casesToRetain = getCaseIdsToExcludeFromDeletion(partitioned.get(RetentionStatus.RETAIN));

        return getDeletableCases(partitioned.get(RetentionStatus.INDETERMINATE), casesToRetain);
    }

    private List<CaseDataEntity> findCandidateCases() {
        final List<CaseDataEntity> expiredCases = getExpiredCases();

        final List<Long> potentialLinkedCaseIds = expiredCases.stream()
            .map(CaseDataEntity::getId)
            .collect(Collectors.toUnmodifiableList());

        final List<CaseLinkEntity> parentCaseLinks = caseLinkRepository.findAllByLinkedCaseId(potentialLinkedCaseIds);

        final Set<Long> caseIds = parentCaseLinks.stream()
            .map(CaseLinkEntity::getCaseId)
            .collect(Collectors.toUnmodifiableSet());

        final List<CaseDataEntity> parentLinkedCases = caseDataRepository.findAllById(caseIds);

        return Stream.of(expiredCases, parentLinkedCases)
            .flatMap(List::stream)
            .collect(Collectors.toMap(
                CaseDataEntity::getId,
                Function.identity(),
                (existing, replacement) -> existing
            ))
            .values().stream()
            .collect(Collectors.toUnmodifiableList());
    }

    private Boolean isExpired(@NonNull final LocalDate caseTtl) {
        final LocalDate today = LocalDate.now();
        return caseTtl.isBefore(today);
    }

    private Boolean isDeletableCaseType(@NonNull final String caseType) {
        return parameterResolver.getDeletableCaseTypes().contains(caseType);
    }

    private Map<RetentionStatus, List<CaseData>> findLinkedCasesAndPartition(final List<CaseDataEntity> candidateCases) {
        final Map<RetentionStatus, List<CaseData>> groupedByStatus = candidateCases.stream()
            .map(entity -> {
                final List<CaseDataEntity> linkedCases = getLinkedCases(entity);
                return isAllDueDeletion(entity, linkedCases)
                    ? buildCaseData(entity, linkedCases, RetentionStatus.INDETERMINATE)
                    : logNonQualifyingCase(entity, linkedCases);
            })
            .collect(Collectors.groupingBy(CaseData::getStatus));

        return Map.of(RetentionStatus.RETAIN, nullCheck(groupedByStatus.get(RetentionStatus.RETAIN)),
                      RetentionStatus.INDETERMINATE, nullCheck(groupedByStatus.get(RetentionStatus.INDETERMINATE)),
                      RetentionStatus.DELETE, nullCheck(groupedByStatus.get(RetentionStatus.DELETE))
        );
    }

    private List<CaseData> nullCheck(final List<CaseData> caseDataList) {
        return Optional.ofNullable(caseDataList).orElse(emptyList());
    }

    private CaseData buildCaseData(final CaseDataEntity masterCase,
                                   final List<CaseDataEntity> linkedCases,
                                   final RetentionStatus status) {
        final List<Long> linkedCaseIds = linkedCases.stream()
            .map(CaseDataEntity::getId)
            .collect(Collectors.toUnmodifiableList());
        return new CaseData(
            masterCase.getId(),
            masterCase.getReference(),
            masterCase.getCaseType(),
            linkedCaseIds,
            status
        );
    }

    private CaseData logNonQualifyingCase(final CaseDataEntity masterCase, final List<CaseDataEntity> linkedCases) {
        final String message = "Not deleting case.reference {}:: "
            + "one or more of the following linked cases.references {} do not meet the criteria for case deletion.";
        final List<Long> linkedCaseIds = linkedCases.stream()
            .map(CaseDataEntity::getReference)
            .collect(Collectors.toUnmodifiableList());
        log.info(message, masterCase.getReference(), linkedCaseIds);

        return buildCaseData(masterCase, linkedCases, RetentionStatus.RETAIN);
    }

    private List<Long> getCaseIdsToExcludeFromDeletion(final List<CaseData> caseDataList) {
        return caseDataList.stream()
            .flatMap(item -> Stream.concat(Stream.of(item.getId()), item.getLinkedCases().stream()))
            .collect(Collectors.toUnmodifiableList());
    }

    private List<CaseData> getDeletableCases(final List<CaseData> indeterminateCases, final List<Long> casesToRetain) {
        return indeterminateCases.stream()
            .filter(item -> !casesToRetain.contains(item.getId()))
            .map(caseData -> new CaseData(
                caseData.getId(),
                caseData.getReference(),
                caseData.getCaseType(),
                caseData.getLinkedCases(),
                RetentionStatus.DELETE
            ))
            .collect(Collectors.toUnmodifiableList());
    }

}
