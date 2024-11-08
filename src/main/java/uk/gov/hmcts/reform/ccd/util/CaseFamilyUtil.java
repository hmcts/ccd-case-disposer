package uk.gov.hmcts.reform.ccd.util;

import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CaseFamilyUtil {

    private CaseFamilyUtil() {
    }

    public static final Function<CaseFamily, List<CaseData>> FLATTEN_CASE_FAMILY_FUNCTION =
        caseFamily -> Stream.of(List.of(caseFamily.getRootCase()), caseFamily.getLinkedCases())
        .flatMap(List::stream)
        .collect(Collectors.toUnmodifiableList());

    public static final Function<CaseFamily, List<CaseData>> POTENTIAL_MULTI_FAMILY_CASE_AGGREGATOR_FUNCTION =
        caseFamily -> caseFamily.getLinkedCases().isEmpty()
            ? List.of(caseFamily.getRootCase())
            : caseFamily.getLinkedCases();
}
