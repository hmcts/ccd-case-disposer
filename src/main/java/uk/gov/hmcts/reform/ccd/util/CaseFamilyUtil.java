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

    /**
     * This function removes duplicate CaseData entries from the list of case families.
     * Duplicate cases may occur when a case belongs to multiple root cases. This ensures
     * that each case is processed only once, avoiding issues like deleting the same case family multiple times.
     */
    public static final Function<List<CaseFamily>, List<CaseData>> POTENTIAL_MULTI_FAMILY_CASE_AGGREGATOR_FUNCTION =
        caseFamilies -> {
            Set<Long> seenIds = new HashSet<>();
            return caseFamilies.stream()
                .flatMap(caseFamily ->
                             caseFamily.getLinkedCases().isEmpty()
                                 ? Stream.of(caseFamily.getRootCase()) : caseFamily.getLinkedCases().stream())
                .filter(caseData -> seenIds.add(caseData.getId()))
                .toList();
        };
}
