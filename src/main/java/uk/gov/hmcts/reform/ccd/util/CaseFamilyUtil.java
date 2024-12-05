package uk.gov.hmcts.reform.ccd.util;

import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.util.List;
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
    public static final Function<List<CaseFamily>, List<List<CaseData>>>
        POTENTIAL_MULTI_FAMILY_CASE_AGGREGATOR_FUNCTION = caseFamilies -> {
            return caseFamilies.stream()
                .map(caseFamily -> caseFamily.getLinkedCases().isEmpty()
                    ? List.of(caseFamily.getRootCase())  // Wrap the root case in a list
                    : caseFamily.getLinkedCases()) // Return linked cases as a list
                .toList();
        };
}






