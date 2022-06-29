package uk.gov.hmcts.reform.ccd.utils;


import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.helper.SecurityUtils;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.DocumentDeletionRecordHolder;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.DOCUMENT_PATH;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.DOCUMENT_STORE_USER;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.SERVICE_AUTHORISATION_HEADER;


@Component
@Slf4j
public class DocumentDeleteTestUtils {

    @Inject
    private DocumentDeletionRecordHolder documentDeletionRecordHolder;

    @Autowired
    private SecurityUtils securityUtils;

    @Inject
    private ParameterResolver parameterResolver;

    @Inject
    private FileUtils fileUtils;

    public void verifyDocumentStoreDeletion(final Map<Long, List<String>> deletableDocuments) {
        deletableDocuments.entrySet().forEach(entry -> {
            final CaseDocumentsDeletionResults caseDocumentsDeletionResults = documentDeletionRecordHolder
                    .getCaseDocumentsDeletionResults(Long.toString(entry.getKey()));

            assertThat(caseDocumentsDeletionResults).isNotNull();
            assertThat(caseDocumentsDeletionResults.getMarkedForDeletion()).isEqualTo(entry.getValue().size());
            assertThat(caseDocumentsDeletionResults.getCaseDocumentsFound()).isEqualTo(entry.getValue().size());
        });

    }

    public void uploadDocument(final Map<Long, List<String>> deletableDocuments) {
        deletableDocuments.entrySet()
                .forEach(entry -> entry.getValue()
                        .forEach(filename -> {
                            try {
                                final Response response = RestAssured
                                        .given()
                                        .relaxedHTTPSValidation()
                                        .baseUri(parameterResolver.getDocumentStoreHost() + DOCUMENT_PATH)
                                        .header(SERVICE_AUTHORISATION_HEADER, securityUtils.getServiceAuthorization())
                                        .header("user-id", DOCUMENT_STORE_USER)
                                        .multiPart("files", fileUtils.getResourceFile(filename), IMAGE_JPEG_VALUE)
                                        .multiPart("classification", "PUBLIC")
                                        .multiPart("metadata[case_id]", Long.toString(entry.getKey()))
                                        .when()
                                        .post()
                                        .andReturn();

                                assertThat(response.getStatusCode()).isEqualTo(200);

                            } catch (final UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }));
    }
}