package uk.gov.hmcts.reform.ccd.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.helper.CaseCreator;
import uk.gov.hmcts.reform.ccd.helper.CcdClientHelper;
import uk.gov.hmcts.reform.ccd.util.log.DataStoreRecordHolder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Component
public class CcdDatastoreTestUtils {

    @Inject
    private CaseCreator caseCreator;

    @Inject
    private DataStoreRecordHolder dataStoreRecordHolder;

    @Inject
    private CcdClientHelper ccdClientHelper;

    public void insertDataIntoCcdDatastore(final Map<String, Integer> initialStateNumberOfDatastoreRecords) {
        initialStateNumberOfDatastoreRecords.forEach((key, value) -> {
            for (int i = 0; i < value; i++) {
                caseCreator.createCase(key);
            }
        });
    }

    public void verifyCcdDatastoreDeletion(final Map<String, Integer> endStateNumberOfDatastoreRecords) {
        final List<String> caseIds = dataStoreRecordHolder.getDatastoreCases().values().stream()
                .flatMap(List::stream)
                .collect(toList());

        final List<CaseDetails> caseDetailsList =
                ccdClientHelper.getCcdCases(caseIds);

        final List<String> resultCaseTypeList = caseDetailsList.stream()
                .map(CaseDetails::getCaseTypeId)
                .collect(toList());

        endStateNumberOfDatastoreRecords.entrySet()
                .forEach(entry -> {
                    final int actualRecordsPerCaseTypes = Collections.frequency(resultCaseTypeList, entry.getKey());
                    assertThat(actualRecordsPerCaseTypes).isEqualTo(entry.getValue());
                });
    }

    public void verifyCcdDatastoreIsPopulated(final Map<String, Integer> initialStateNumberOfDatastoreRecords) {
        assertThat(dataStoreRecordHolder.getDatastoreCases().size())
                .isEqualTo(initialStateNumberOfDatastoreRecords.size());
    }
}