package uk.gov.hmcts.reform.ccd.util;

import jakarta.inject.Named;
import uk.gov.hmcts.reform.ccd.data.model.CaseData;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;


@Named
public class LogAndAuditCaseFilter {

    public List<CaseData> getDistinctCaseDataFromCaseFamilyList(final List<CaseFamily> deletableLinkedFamilies) {
        final List<CaseData> linkedCases = getLinkedCaseData(deletableLinkedFamilies);
        final List<CaseData> rootCases = getRootCaseData(deletableLinkedFamilies);

        //Remove potential linked case duplicates
        return Stream.concat(linkedCases.stream(), rootCases.stream())
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(CaseData::getReference))),
                        ArrayList::new));
    }

    private List<CaseData> getRootCaseData(List<CaseFamily> deletableLinkedFamilies) {
        return deletableLinkedFamilies
                .stream()
                .map(caseFamily -> caseFamily.getRootCase())
                .collect(toList());
    }

    private List<CaseData> getLinkedCaseData(List<CaseFamily> deletableLinkedFamilies) {
        return deletableLinkedFamilies.stream()
                .flatMap(caseFamily -> caseFamily
                        .getLinkedCases()
                        .stream())
                .collect(toList());
    }
}
