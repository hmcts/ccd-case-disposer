package uk.gov.hmcts.reform.ccd.shell.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.ccd.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.data.CcdCaseSearchResponse;
import uk.gov.hmcts.reform.ccd.shell.service.client.CcdClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShellCaseFinder {

    private final SecurityUtil securityUtil;
    private final CcdClient ccdClient;
    private static final int NUMBER_OF_RESULTS = 5;

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public Optional<Long> findShellCase(String shellCaseType, long originalCaseReference) {
        ObjectNode request = buildSearchRequest(String.valueOf(originalCaseReference));
        try {
            CcdCaseSearchResponse response = ccdClient.searchCase(
                securityUtil.getServiceAuthorization(),
                securityUtil.getIdamClientToken(),
                shellCaseType,
                request
            );
            if (response == null || response.cases() == null) {
                throw new ShellCaseException(
                    "CCD returned invalid shell case search response while searching for " + originalCaseReference);
            }
            if (response.total() > 1) {
                log.error("Found multiple shell cases for original case reference {}", originalCaseReference);
            }
            return response.cases().stream()
                .map(CcdCaseSearchResponse.EsCase::reference)
                .filter(Objects::nonNull)
                .findFirst();
        } catch (FeignException exc) {
            throw new ShellCaseException("failed to search for case" + originalCaseReference, exc);
        }
    }

    private ObjectNode buildSearchRequest(String originalCaseReference) {
        // {"query": {
        //     "term": {
        //         "data.original_case_reference.keyword": originalCaseReference
        //      }
        // }, "size": 5}
        ObjectNode query = JsonNodeFactory.instance.objectNode();
        String propertyName = "data." + ShellCaseCreator.ORIGINAL_CASE_REFERENCE + ".keyword";
        query
            .put("size", NUMBER_OF_RESULTS)
            .putObject("query")
            .putObject("term")
            .put(propertyName, originalCaseReference);

        return query;
    }
}
