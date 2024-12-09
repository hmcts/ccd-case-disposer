package uk.gov.hmcts.reform.ccd.util;

import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class CaseFamilyUtil {

    private CaseFamilyUtil() {
    }

    public static final Function<List<CaseFamily>, List<CaseData>> FLATTEN_CASE_FAMILIES_FUNCTION = caseFamilies ->
        caseFamilies.stream()
            .flatMap(caseFamily ->
                         Stream.concat(Stream.of(caseFamily.getRootCase()), caseFamily.getLinkedCases().stream()))
            .toList();

    public static final Function<List<CaseFamily>, List<CaseData>> FLATTEN_CASE_FAMILIES_AND_REMOVE_DUPLICATE_FUNCTION =
        caseFamilies -> {
            Set<Long> seenIds = new HashSet<>();
            return caseFamilies.stream()
                .flatMap(caseFamily ->
                             Stream.concat(Stream.of(caseFamily.getRootCase()), caseFamily.getLinkedCases().stream()))
                .filter(caseData -> seenIds.add(caseData.getId()))
                .toList();
        };

    public static final Function<List<CaseFamily>, List<List<CaseData>>>
        POTENTIAL_MULTI_FAMILY_CASE_AGGREGATOR_FUNCTION = caseFamilies -> caseFamilies.stream()
            .map(caseFamily -> caseFamily.getLinkedCases().isEmpty()
                ? List.of(caseFamily.getRootCase())  // Wrap the root case in a list
                : caseFamily.getLinkedCases()) // Return linked cases as a list
            .toList();
}






