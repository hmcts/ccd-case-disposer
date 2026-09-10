package uk.gov.hmcts.reform.ccd.shell.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.JsonNodeFactory;
import uk.gov.hmcts.reform.ccd.shell.data.CaseDocumentHashResponse;
import uk.gov.hmcts.reform.ccd.shell.model.ShellDocument;
import uk.gov.hmcts.reform.ccd.shell.service.client.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentHashServiceTest {

    private static final String SERVICE_TOKEN = "service-token";
    private static final String USER_TOKEN = "user-token";
    private static final UUID FIRST_DOCUMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SECOND_DOCUMENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private CaseDocumentClient documentClient;
    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private DocumentHashService underTest;

    @Test
    void shouldFetchHashOnceForEachUniqueDocument() {
        ShellDocument first = document(FIRST_DOCUMENT_ID);
        ShellDocument duplicate = document(FIRST_DOCUMENT_ID);
        ShellDocument second = document(SECOND_DOCUMENT_ID);
        when(securityUtil.getServiceAuthorization()).thenReturn(SERVICE_TOKEN);
        when(securityUtil.getIdamClientToken()).thenReturn(USER_TOKEN);
        when(documentClient.getDocumentHash(SERVICE_TOKEN, USER_TOKEN, FIRST_DOCUMENT_ID))
            .thenReturn(new CaseDocumentHashResponse("first-hash"));
        when(documentClient.getDocumentHash(SERVICE_TOKEN, USER_TOKEN, SECOND_DOCUMENT_ID))
            .thenReturn(new CaseDocumentHashResponse("second-hash"));

        Map<UUID, String> result = underTest.fetchHashes(List.of(first, duplicate, second));

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
            FIRST_DOCUMENT_ID, "first-hash",
            SECOND_DOCUMENT_ID, "second-hash"
        ));
        verify(documentClient, times(1)).getDocumentHash(SERVICE_TOKEN, USER_TOKEN, FIRST_DOCUMENT_ID);
        verify(documentClient, times(1)).getDocumentHash(SERVICE_TOKEN, USER_TOKEN, SECOND_DOCUMENT_ID);
    }

    @Test
    void shouldReturnEmptyMapWhenThereAreNoDocuments() {
        Map<UUID, String> result = underTest.fetchHashes(List.of());

        assertThat(result).isEmpty();
    }

    private ShellDocument document(UUID documentId) {
        return new ShellDocument(documentId, JsonNodeFactory.instance.objectNode());
    }
}
