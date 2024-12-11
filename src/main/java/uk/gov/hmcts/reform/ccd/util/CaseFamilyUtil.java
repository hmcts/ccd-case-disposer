package uk.gov.hmcts.reform.ccd.util;

import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CaseFamilyUtil {

    private CaseFamilyUtil() {
    }

//    public static Set<CaseData> getRoots(final List<CaseFamily> caseFamilies) {
//        return caseFamilies.stream()
//            .map(CaseFamily::getRootCase)
//            .collect(Collectors.toSet());
//    }

    public static Set<CaseData> getCaseData(final List<CaseFamily> caseFamilies) {
        return  caseFamilies.stream()
            .flatMap(caseFamily ->
                         Stream.concat(Stream.of(caseFamily.getRootCase()), caseFamily.getLinkedCases().stream()))
            .collect(Collectors.toSet());
    }
}






