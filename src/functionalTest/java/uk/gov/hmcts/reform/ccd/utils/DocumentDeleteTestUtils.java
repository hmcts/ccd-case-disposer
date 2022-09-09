package uk.gov.hmcts.reform.ccd.utils;


import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.helper.SecurityUtils;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.DataStoreRecordHolder;
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

    @Inject
    private DataStoreRecordHolder dataStoreRecordHolder;

    @Inject
    private SecurityUtils securityUtils;

    @Inject
    private ParameterResolver parameterResolver;

    @Inject
    private FileUtils fileUtils;

    public void verifyDocumentStoreDeletion(final Map<String, String> deletableDocuments) {
        deletableDocuments.entrySet().forEach(entry -> {
            final List<String> deletedDocumentCaseRefList =
                    dataStoreRecordHolder.getDatastoreCases().get(entry.getKey());

            deletedDocumentCaseRefList.forEach(deletedDocumentCaseRef -> {
                final CaseDocumentsDeletionResults caseDocumentsDeletionResults = documentDeletionRecordHolder
                        .getCaseDocumentsDeletionResults(deletedDocumentCaseRef);

                assertThat(caseDocumentsDeletionResults).isNotNull();
                assertThat(caseDocumentsDeletionResults.getMarkedForDeletion()).isNotZero();
                assertThat(caseDocumentsDeletionResults.getCaseDocumentsFound()).isNotZero();
            });
        });
    }

    public void uploadDocument(final Map<String, String> deletableDocuments) {
        deletableDocuments.entrySet().forEach(entry -> dataStoreRecordHolder.getDatastoreCases().get(entry.getKey())
                .forEach(caseId -> {
                    try {
                        final Response response = RestAssured
                                .given()
                                .relaxedHTTPSValidation()
                                .baseUri(parameterResolver.getDocumentStoreHost() + DOCUMENT_PATH)
                                .header(SERVICE_AUTHORISATION_HEADER, securityUtils.getServiceAuthorization())
                                .header("user-id", DOCUMENT_STORE_USER)
                                .multiPart("files", fileUtils.getResourceFile(entry.getValue()),
                                        IMAGE_JPEG_VALUE)
                                .multiPart("classification", "PUBLIC")
                                .multiPart("metadata[case_id]", caseId)
                                .when()
                                .post()
                                .andReturn();

                        assertThat(response.getStatusCode()).isEqualTo(200);

                    } catch (final UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                })
        );
    }
}