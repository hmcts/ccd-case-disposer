package uk.gov.hmcts.reform.ccd.service;

import lombok.NonNull;
import uk.gov.hmcts.reform.ccd.data.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;
import uk.gov.hmcts.reform.ccd.data.model.CaseTreeNode;
import uk.gov.hmcts.reform.ccd.data.model.LinkedEntities;
import uk.gov.hmcts.reform.ccd.exception.CaseDataNotFound;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.ccd.util.ListUtil.distinctByKey;

@Named
public class CaseFamilyTreeService {
    private static final String CASE_DATA_NOT_FOUND = "Case data for case_id=%d is not found";
    private final CaseDataRepository caseDataRepository;
    private final CaseLinkRepository caseLinkRepository;
    private final ParameterResolver parameterResolver;
    private final Function<LinkedEntities<CaseDataEntity>, CaseTreeNode> parentNodesFunction = this::findParents;

    @Inject
    public CaseFamilyTreeService(final CaseDataRepository caseDataRepository,
                                 final CaseLinkRepository caseLinkRepository,
                                 final ParameterResolver parameterResolver) {
        this.caseDataRepository = caseDataRepository;
        this.caseLinkRepository = caseLinkRepository;
        this.parameterResolver = parameterResolver;
    }

    private final Function<LinkedEntities<CaseData>, List<CaseData>> familyMembersFunction = this::findFamilyMembers;

    public List<CaseFamily> getCaseFamilies() {
        final List<CaseDataEntity> rootNodes = getRootNodes();

        return rootNodes.stream()
                .map(this::buildCaseFamily)
                .collect(Collectors.toUnmodifiableList());
    }

    private final Function<CaseTreeNode, Set<CaseTreeNode>> leafNodesFunction = this::findLeafNodes;

    private List<CaseDataEntity> getExpiredCases() {
        return caseDataRepository.findExpiredCases(parameterResolver.getAllDeletableCaseTypes());
    }

    private CaseTreeNode findParents(@NonNull final LinkedEntities<CaseDataEntity> linkedEntities) {
        final CaseDataEntity caseDataEntity = linkedEntities.getData();
        final List<CaseLinkEntity> parentEntities = caseLinkRepository.findByLinkedCaseId(caseDataEntity.getId());

        final List<CaseTreeNode> parentNodes = parentEntities.stream()
                .filter(distinctByKey(CaseLinkEntity::getCaseId))
                .filter(entity -> cyclicallyLinked(linkedEntities.getLinks(), entity.getCaseId()))
                .map(parentEntity -> {
                    final Long caseId = parentEntity.getCaseId();
                    final Optional<CaseDataEntity> entity = caseDataRepository.findById(caseId);
                    return entity.map(parentCase -> {
                        final List<Long> descendants = Stream.of(linkedEntities.getLinks(),
                                        List.of(caseDataEntity.getId()))
                                .flatMap(Collection::stream)
                                .collect(Collectors.toUnmodifiableList());

                        final LinkedEntities<CaseDataEntity> linked = new LinkedEntities<>(
                                parentCase,
                                linkedEntities.getFamilyId(),
                                descendants
                        );
                        return parentNodesFunction.apply(linked);
                    }).orElseThrow(() -> new CaseDataNotFound(String.format(CASE_DATA_NOT_FOUND, caseId)));
                })
                .collect(Collectors.toUnmodifiableList());

        return new CaseTreeNode(caseDataEntity, parentNodes);
    }

    private Set<CaseTreeNode> findLeafNodes(@NonNull final CaseTreeNode node) {
        final List<CaseTreeNode> parents = node.getParentNodes();

        return parents.isEmpty()
                ? Set.of(node)
                : parents.stream()
                .map(leafNodesFunction)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    private List<CaseData> findFamilyMembers(@NonNull final LinkedEntities<CaseData> linkedEntities) {
        final CaseData caseNode = linkedEntities.getData();
        final List<CaseData> members = new ArrayList<>();
        final List<CaseLinkEntity> caseLinkEntities = caseLinkRepository.findByCaseId(caseNode.getId());
        final List<CaseData> descendants = caseLinkEntities.stream()
                .filter(x -> cyclicallyLinked(linkedEntities.getLinks(), x.getLinkedCaseId()))
                .map(caseLink -> {
                    final Long linkedCaseId = caseLink.getLinkedCaseId();
                    final Optional<CaseDataEntity> childEntity = caseDataRepository.findById(linkedCaseId);
                    return childEntity
                            .map(entity -> {
                                final List<Long> ancestors = Stream.of(linkedEntities.getLinks(),
                                                List.of(caseNode.getId()))
                                        .flatMap(Collection::stream)
                                        .collect(Collectors.toUnmodifiableList());

                                final CaseData caseData = new CaseData(
                                        entity.getId(),
                                        entity.getReference(),
                                        entity.getCaseType(),
                                        entity.getResolvedTtl(),
                                        linkedEntities.getFamilyId(),
                                        caseNode
                                );
                                members.add(caseData);

                                final LinkedEntities<CaseData> linked = new LinkedEntities<>(
                                        caseData,
                                        linkedEntities.getFamilyId(),
                                        ancestors
                                );
                                return familyMembersFunction.apply(linked);
                            })
                            .orElseThrow(() -> new CaseDataNotFound(String.format(CASE_DATA_NOT_FOUND, linkedCaseId)));
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableList());

        members.addAll(descendants);
        return members.stream()
                .filter(distinctByKey(CaseData::getId))
                .sorted(Comparator.comparing(CaseData::getId))
                .collect(Collectors.toUnmodifiableList());
    }

    private Boolean cyclicallyLinked(@NonNull final List<Long> links, @NonNull final Long term) {
        return !links.contains(term);
    }

    List<CaseDataEntity> getRootNodes() {
        final List<CaseDataEntity> expiredCases = getExpiredCases();

        final Set<CaseDataEntity> roots = expiredCases.stream()
                .map(caseDataEntity -> {
                    final LinkedEntities<CaseDataEntity> linkedEntities = new LinkedEntities<>(
                            caseDataEntity,
                            null,
                            emptyList()
                    );
                    return parentNodesFunction.apply(linkedEntities);
                })
                .map(leafNodesFunction)
                .flatMap(Collection::stream)
                .map(CaseTreeNode::getCaseNode)
                .collect(Collectors.toUnmodifiableSet());

        return roots.stream()
                .sorted(Comparator.comparing(CaseDataEntity::getId))
                .collect(Collectors.toUnmodifiableList());
    }

    CaseFamily buildCaseFamily(@NonNull final CaseDataEntity caseNode) {
        final CaseData rootCaseData = new CaseData(
                caseNode.getId(),
                caseNode.getReference(),
                caseNode.getCaseType(),
                caseNode.getResolvedTtl(),
                caseNode.getId(),
                null
        );
        final LinkedEntities<CaseData> linkedEntities = new LinkedEntities<>(
                rootCaseData,
                caseNode.getId(),
                emptyList()
        );

        final List<CaseData> familyMembers = familyMembersFunction.apply(linkedEntities);

        return new CaseFamily(rootCaseData, familyMembers);
    }


}
