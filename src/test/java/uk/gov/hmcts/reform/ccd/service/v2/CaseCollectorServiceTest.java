package uk.gov.hmcts.reform.ccd.service.v2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.CaseDataRepository;
import uk.gov.hmcts.reform.ccd.data.CaseLinkRepository;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.data.entity.CaseLinkEntity;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.exception.JobInterruptedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CaseCollectorServiceTest {

    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private CaseLinkRepository caseLinkRepository;

    @InjectMocks
    private CaseCollectorService service;

    @Test
    void getDeletableCases_ReturnsEmpty_WhenNoExpiredCases() {
        given(caseDataRepository.findExpiredCases(anyList())).willReturn(List.of());
        Set<CaseData> result = service.getDeletableCases(List.of("MyType", "AnotherType"));
        assertThat(result).isEmpty();
    }

    @Test
    void getDeletableCases_ReturnsAll_WhenAllLinksExpired() {
        List<CaseDataEntity> expired = List.of(entity(1), entity(2), entity(3));
        given(caseDataRepository.findExpiredCases(anyList())).willReturn(expired);
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(1L))).willReturn(List.of(link(1, 2)));
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(2L))).willReturn(List.of(link(2, 3)));

        Set<CaseData> result = service.getDeletableCases(List.of("MyType", "AnotherType"));

        assertThat(result.stream().map(CaseData::getId)).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void getDeletableCases_ExcludesCasesLinkedToNonExpired() {
        List<CaseDataEntity> expired = List.of(entity(1), entity(2));
        given(caseDataRepository.findExpiredCases(anyList())).willReturn(expired);
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(1L))).willReturn(List.of(link(1, 99)));

        // when
        Set<CaseData> result = service.getDeletableCases(List.of("TYPE"));

        // then
        assertThat(result.stream().map(CaseData::getId)).containsExactly(2L);
    }

    @Test
    void getDeletableCases_TraversesTransitively() {
        List<CaseDataEntity> expired = List.of(entity(1), entity(2), entity(3), entity(4));
        given(caseDataRepository.findExpiredCases(anyList())).willReturn(expired);
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(1L))).willReturn(List.of(link(1, 2)));
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(2L))).willReturn(List.of(link(2, 99)));
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(3L))).willReturn(List.of(link(3, 4)));
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(4L))).willReturn(List.of(link(3, 4)));

        // when
        Set<CaseData> result = service.getDeletableCases(List.of("TYPE"));

        // then
        assertThat(result.stream().map(CaseData::getId)).containsExactlyInAnyOrder(3L, 4L);
    }

    @Test
    void getDeletableCases_HandlesSelfLinks() {
        given(caseDataRepository.findExpiredCases(anyList())).willReturn(List.of(entity(1), entity(2)));
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(1L))).willReturn(List.of(link(1, 1)));
        Set<CaseData> result = service.getDeletableCases(List.of("TYPE"));
        assertThat(result.stream().map(CaseData::getId)).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void getDeletableCases_HandlesCyclicalLinks() {
        given(caseDataRepository.findExpiredCases(anyList())).willReturn(List.of(entity(1), entity(2), entity(3)));
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(1L))).willReturn(List.of(link(1, 2)));
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(2L))).willReturn(List.of(link(2, 3)));
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(3L))).willReturn(List.of(link(3, 1)));
        Set<CaseData> result = service.getDeletableCases(List.of("TYPE"));
        assertThat(result.stream().map(CaseData::getId)).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void getDeletableCases_HandlesCyclicalLinkWithNonExpired() {
        given(caseDataRepository.findExpiredCases(anyList())).willReturn(List.of(entity(1), entity(2), entity(3)));
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(1L))).willReturn(List.of(link(1, 2)));
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(2L))).willReturn(List.of(link(2, 3)));
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(3L))).willReturn(
            List.of(link(3, 1), link(99, 3)));
        Set<CaseData> result = service.getDeletableCases(List.of("TYPE"));
        assertThat(result).isEmpty();
    }

    @Test
    void getDeletableCases_HandlesManyLinks() {
        given(caseDataRepository.findExpiredCases(anyList())).willReturn(List.of(entity(1), entity(2), entity(3)));
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(1L))).willReturn(List.of(
            link(1, 2),
            link(1, 3),
            link(1, 4),
            link(1, 5)
        ));
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(2L))).willReturn(List.of(link(1, 2)));
        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(Set.of(3L))).willReturn(List.of(link(1, 3)));
        Set<CaseData> result = service.getDeletableCases(List.of("TYPE"));
        assertThat(result).isEmpty();
    }

    @Test
    void getDeletableCases_ReturnsEmptyIfNoDeletableCaseTypesSet() {
        Set<CaseData> result = service.getDeletableCases(null);
        assertThat(result).isEmpty();
        verify(caseDataRepository, times(0)).findExpiredCases(any());

        result = service.getDeletableCases(List.of());
        assertThat(result).isEmpty();
        verify(caseDataRepository, times(0)).findExpiredCases(any());
    }

    @Test
    void getDeletableCases_ThrowsJobInterruptedException_WhenThreadInterruptedAtStart() {
        Thread.currentThread().interrupt();
        given(caseDataRepository.findExpiredCases(anyList())).willReturn(List.of(entity(1)));

        assertThatThrownBy(() -> service.getDeletableCases(List.of("TYPE")))
            .isInstanceOf(JobInterruptedException.class);

        // clear interruption for other tests
        Thread.interrupted();
    }

    @Test
    void getDeletableCases_ThrowsJobInterruptedException_DuringLinkTraversal() {
        AtomicBoolean firstCalled = new AtomicBoolean(true);
        // not interrupted at start, but will be during traversal
        given(caseDataRepository.findExpiredCases(anyList())).willReturn(List.of(entity(1), entity(2)));

        given(caseLinkRepository.findByCaseIdInOrLinkedCaseIdIn(anySet()))
            .willAnswer(inv -> {
                if (firstCalled.getAndSet(false)) {
                    return List.of(link(1, 2), link(2, 3));
                } else {
                    Thread.currentThread().interrupt();
                    return List.of(link(1, 2));
                }
            });

        assertThatThrownBy(() -> service.getDeletableCases(List.of("TYPE")))
            .isInstanceOf(JobInterruptedException.class);

        // clear interrupt flag
        Thread.interrupted();
    }

    private static CaseDataEntity entity(long id) {
        CaseDataEntity e = new CaseDataEntity();
        e.setId(id);
        e.setReference(id);
        e.setCaseType("TYPE");
        e.setJurisdiction("JUR");
        e.setResolvedTtl(LocalDate.now().minusDays(1));
        return e;
    }

    private static CaseLinkEntity link(long left, long right) {
        CaseLinkEntity l = new CaseLinkEntity();
        l.setCaseId(left);
        l.setLinkedCaseId(right);
        return l;
    }

}
