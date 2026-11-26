package uk.gov.hmcts.reform.ccd.utils;


import io.restassured.RestAssured;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.data.em.DocumentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.data.entity.CaseDataEntity;
import uk.gov.hmcts.reform.ccd.helper.SecurityUtils;
import uk.gov.hmcts.reform.ccd.model.DocumentField;
import uk.gov.hmcts.reform.ccd.model.StoredDocumentHalResourceCollection;
import uk.gov.hmcts.reform.ccd.util.log.DocumentDeletionRecordHolder;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.with;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.DELETE_DOCUMENT_PATH;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.DOCUMENT_PATH;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.DOCUMENT_STORE_USER;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.SERVICE_AUTHORISATION_HEADER;


@Component
@Slf4j
public class DocumentDeleteTestUtils {

    @Value("${remote.document.store.host}")
    private String documentStoreHost;

    @Inject
    private DocumentDeletionRecordHolder documentDeletionRecordHolder;

    @Inject
    private SecurityUtils securityUtils;

    @Inject
    private FileUtils fileUtils;

    public void verifyDocumentStoreDeletion(final Map<Long, List<String>> deletableDocuments) {
        with().await()
                .untilAsserted(() -> deletableDocuments.entrySet().forEach(entry -> {
                    final CaseDocumentsDeletionResults caseDocumentsDeletionResults = documentDeletionRecordHolder
                            .getCaseDocumentsDeletionResults(Long.toString(entry.getKey()));

                    assertThat(caseDocumentsDeletionResults).isNotNull();
                    assertThat(caseDocumentsDeletionResults.getMarkedForDeletion()).isNotZero();
                    assertThat(caseDocumentsDeletionResults.getCaseDocumentsFound()).isNotZero();
                }));
    }

    public void verifyOriginalCaseDocumentHasMoved(final Map<Long, List<String>> deletableDocuments) {
        with().await()
            .untilAsserted(() -> deletableDocuments.entrySet().forEach(entry -> {
                final CaseDocumentsDeletionResults caseDocumentsDeletionResults = documentDeletionRecordHolder
                    .getCaseDocumentsDeletionResults(Long.toString(entry.getKey()));
                assertThat(caseDocumentsDeletionResults).isNotNull();
                assertThat(caseDocumentsDeletionResults.getMarkedForDeletion()).isZero();
                assertThat(caseDocumentsDeletionResults.getCaseDocumentsFound()).isZero();
            }));
    }

    public Map<Long, List<DocumentField>> uploadDocument(final Map<Long, List<String>> deletableDocuments,
                               String caseTypeId, String jurisdiction) {
        Map<Long, List<DocumentField>> uploadedDocuments = new ConcurrentHashMap<>(deletableDocuments.size());
        deletableDocuments.forEach((key, value) -> value
            .forEach(filename -> {
                try {
                    final Response response = RestAssured
                        .given()
                        .relaxedHTTPSValidation()
                        .baseUri(documentStoreHost + DOCUMENT_PATH)
                        .header(SERVICE_AUTHORISATION_HEADER, securityUtils.getServiceAuthorization())
                        .header("user-id", DOCUMENT_STORE_USER)
                        .multiPart("files", fileUtils.getResourceFile(filename), IMAGE_JPEG_VALUE)
                        .multiPart("classification", "PUBLIC")
                        .multiPart("metadata[case_id]", Long.toString(key))
                        .multiPart("metadata[case_type_id]", caseTypeId)
                        .multiPart("metadata[jurisdiction]", jurisdiction)
                        .when()
                        .post()
                        .andReturn();

                    assertThat(response.getStatusCode()).isEqualTo(200);
                    uploadedDocuments.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(buildDocumentField(response));

                } catch (final UnsupportedEncodingException e) {
                    log.error("Error while uploading document {}", filename, e);
                }
            }));
        return uploadedDocuments;

    }

    private DocumentField buildDocumentField(Response response) {
        StoredDocumentHalResourceCollection collection =
            response.as(StoredDocumentHalResourceCollection.class);

        StoredDocumentHalResourceCollection.StoredDocument document =
            collection.embedded().documents().getFirst();

        StoredDocumentHalResourceCollection.Version version = document
            .embedded()
            .allDocumentVersions()
            .embedded()
            .documentVersions()
            .getFirst();

        return new DocumentField(
            document.links().self().href(),
            document.links().binary().href(),
            version.originalDocumentName(),
            version.createdOn()
        );
    }

    public void deleteDocument(final String caseRef) {
        final DocumentsDeletePostRequest documentsDeleteRequest = new DocumentsDeletePostRequest(caseRef);
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(documentStoreHost + DELETE_DOCUMENT_PATH)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORISATION_HEADER,
                    securityUtils.getServiceAuthorization())
            .request().body(documentsDeleteRequest)
            .post()
            .andReturn();
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    public void verifyShellCaseDocument(final CaseDataEntity caseDataEntity,
                                        final Map<Long, List<String>> deletableDocuments) {
        Map.Entry<Long, List<String>> firstEntry =
            deletableDocuments.entrySet().iterator().next();
        String fileName = firstEntry.getValue().getFirst();
        assertThat(caseDataEntity.getData().contains(fileName)).isTrue();
    }
}
