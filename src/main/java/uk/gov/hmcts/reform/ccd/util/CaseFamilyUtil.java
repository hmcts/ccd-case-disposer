package uk.gov.hmcts.reform.ccd.util;

import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CaseFamilyUtil {

    private CaseFamilyUtil() {
    }

    /**
     * Flatten case families into a set of case data.
     *
     * @param caseFamilies the case families
     * @return the set with CaseData entries
     */
    public static Set<CaseData> getCaseData(final List<CaseFamily> caseFamilies) {
        return  caseFamilies.stream()
            .map(CaseFamily::linkedCases)
            .flatMap(List::stream)
            .collect(Collectors.toSet());
    }
}
