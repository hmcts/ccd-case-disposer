package uk.gov.hmcts.reform.ccd.utils;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.HearingDeletionRecordHolder;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Component
@Slf4j
public class HearingDocumentDeleteTestUtils {

    @Inject
    private HearingDeletionRecordHolder hearingDeletionRecordHolder;

    @Inject
    private ParameterResolver parameterResolver;

    public void verifyHearingDocumentStoreDeletion(final Map<String, List<Long>> indexedData) {
        indexedData.forEach((key, value) -> {
            if (key.equals(parameterResolver.getHearingCaseType())) {
                value.forEach(caseReference -> {
                    int status = hearingDeletionRecordHolder
                            .getHearingDeletionResults(Long.toString(caseReference));

                    assertEquals("Status does not match", NO_CONTENT.value(), status);
                });
                hearingDeletionRecordHolder.getHearingDeletionRecordHolderList().clear();
            }
        });
    }

}
