package uk.gov.hmcts.reform.ccd.utils;

import jakarta.inject.Inject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.LauRecordHolder;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.with;

@Component
public class LauTestUtils {

    @Inject
    private ParameterResolver parameterResolver;

    @Inject
    private LauRecordHolder lauRecordHolder;

    public void verifyLauLogs(final List<List<Long>> deletableEndStateRowIds) {

        final List<Long> caseRefs = deletableEndStateRowIds.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (parameterResolver.isLogAndAuditEnabled()) {
            with().await()
                    .untilAsserted(() -> assertThat(lauRecordHolder.getLauCaseRefList())
                            .containsExactlyInAnyOrderElementsOf(transform(newArrayList(caseRefs),
                                    toStringFunction())));

            lauRecordHolder.getLauCaseRefList().clear();
        }
    }
}
