package uk.gov.hmcts.reform.ccd.util;

import lombok.Getter;
import uk.gov.hmcts.reform.ccd.data.model.CaseFamily;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Named;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toSet;

@Named
@Getter
public class FailedToDeleteCaseFamilyHolder {

    private List<CaseFamily> failedToDeleteCaseFamilies = new ArrayList<>();
    private List<List<Long>> caseRefs = new ArrayList<>();

    public void addCaseFamily(final CaseFamily caseFamily) {
        final Set<Long> familyCaseRefs =
                Stream.of(List.of(caseFamily.getRootCase()), caseFamily.getLinkedCases())
                        .flatMap(Collection::stream)
                        .map(value -> value.getReference())
                        .collect(toSet());

        caseRefs.add(newArrayList(familyCaseRefs));
        failedToDeleteCaseFamilies.add(caseFamily);
    }
}
