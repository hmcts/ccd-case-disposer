package uk.gov.hmcts.reform.ccd.util;

import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CaseFamilyUtil {

    private CaseFamilyUtil() {
    }

    private static final Function<CaseFamily, List<CaseData>> FLATTEN_FUNCTION = caseFamily ->
        Stream.of(List.of(caseFamily.getRootCase()), caseFamily.getLinkedCases())
            .flatMap(List::stream)
            .collect(Collectors.toUnmodifiableList());

    private static final Function<List<CaseFamily>, Function<CaseData, List<Long>>> AGGREGATE_FUNCTION =
        caseFamilies -> linkedCase -> {
            final List<CaseData> allCases = caseFamilies.stream()
                .map(FLATTEN_FUNCTION)
                .flatMap(List::stream)
                .collect(Collectors.toUnmodifiableList());

            return allCases.stream()
                .filter(caseData -> caseData.getId().equals(linkedCase.getId()))
                .map(CaseData::getFamilyId)
                .collect(Collectors.toUnmodifiableList());
        };

    public static final BiFunction<List<CaseFamily>, List<CaseData>, List<CaseFamily>> LINKED_FAMILIES_FUNCTION =
        (caseFamilies, linkedCases) -> {
            final Function<List<CaseData>, List<Long>> linkedFamilyIdsFunction = linked -> linked.stream()
                .map(caseData -> AGGREGATE_FUNCTION.apply(caseFamilies).apply(caseData))
                .flatMap(List::stream)
                .collect(Collectors.toUnmodifiableList());

            final Function<List<Long>, List<CaseFamily>> linkedFamiliesFunction =
                linkedFamilyIds -> caseFamilies.stream()
                    .filter(caseFamily -> linkedFamilyIds.contains(caseFamily.getRootCase().getFamilyId()))
                    .collect(Collectors.toUnmodifiableList());

            return linkedFamilyIdsFunction.andThen(linkedFamiliesFunction)
                .apply(linkedCases);
        };

}
