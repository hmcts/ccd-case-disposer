package uk.gov.hmcts.reform.ccd.util.log;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Getter
public class DataStoreRecordHolder {
    private Map<String, List<String>> datastoreCases = new HashMap<>();

    public void addRecord(final String caseType, final String caseId) {
        if (!datastoreCases.containsKey(caseType)) {
            datastoreCases.put(caseType, new ArrayList<>());
        }
        datastoreCases.get(caseType).add(caseId);
    }
}