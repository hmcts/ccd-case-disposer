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

    private static final BiFunction<List<CaseFamily>, Function<CaseFamily, List<CaseData>>, List<CaseData>>
        AGGREGATE_FUNCTION = (caseFamilies, func) -> caseFamilies.stream()
        .map(func)
        .flatMap(List::stream)
        .collect(Collectors.toUnmodifiableList());

    public static final Function<List<CaseFamily>, List<CaseData>> FLATTEN_CASE_FAMILIES_FUNCTION = caseFamilies -> {
        final Function<CaseFamily, List<CaseData>> func = caseFamily ->
            Stream.of(List.of(caseFamily.getRootCase()), caseFamily.getLinkedCases())
                .flatMap(List::stream)
                .collect(Collectors.toUnmodifiableList());

        return AGGREGATE_FUNCTION.apply(caseFamilies, func);
    };

    public static final Function<List<CaseFamily>, List<CaseData>> POTENTIAL_MULTI_FAMILY_CASE_AGGREGATOR_FUNCTION =
        caseFamilies -> {
            final Function<CaseFamily, List<CaseData>> func = caseFamily ->
                caseFamily.getLinkedCases().isEmpty()
                    ? List.of(caseFamily.getRootCase())
                    : caseFamily.getLinkedCases();

            return AGGREGATE_FUNCTION.apply(caseFamilies, func);
        };
}
