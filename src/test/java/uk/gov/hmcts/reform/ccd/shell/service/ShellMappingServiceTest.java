package uk.gov.hmcts.reform.ccd.shell.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.shell.config.ShellCaseProperties;
import uk.gov.hmcts.reform.ccd.shell.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.model.ShellMappingResponse;
import uk.gov.hmcts.reform.ccd.shell.service.client.ShellMappingClient;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.TooManyMethods")
class ShellMappingServiceTest {

    private static final String SERVICE_TOKEN = "service-token";
    private static final String IDAM_TOKEN = "idam-token";
    private static final String STATE_CATEGORY_TO_EXCLUDE = "draft";
    private static final String CASE_TYPE_ID = "case-type-id";

    @Mock
    private ShellMappingClient shellMappingClient;

    @Mock
    private SecurityUtil securityUtil;

    private ShellMappingService shellMappingService;

    @BeforeEach
    void setUp() {
        ShellCaseProperties shellCaseProps = new ShellCaseProperties();
        shellCaseProps.setEnabled(true);
        shellCaseProps.setDraftStateCategory(STATE_CATEGORY_TO_EXCLUDE);
        shellMappingService = new ShellMappingService(shellMappingClient, securityUtil, shellCaseProps);
    }

    @Test
    void shouldReturnMissingMappingResponseWhenShellMappingDoesNotExist() {
        stubAuthTokens();
        ShellMappingResponse missingMapping = new ShellMappingResponse(null, List.of(), null);
        when(shellMappingClient.getShellMappings(SERVICE_TOKEN, IDAM_TOKEN, STATE_CATEGORY_TO_EXCLUDE, CASE_TYPE_ID))
            .thenReturn(missingMapping);

        ShellMappingResponse response = shellMappingService.loadMappings(CASE_TYPE_ID);

        assertThat(response).isSameAs(missingMapping);
    }


    @Test
    void shouldReturnClientResponseWhenShellMappingExists() {
        stubAuthTokens();
        ShellMappingResponse expectedResponse = new ShellMappingResponse("shell-case-type-id", List.of(), List.of());
        when(shellMappingClient.getShellMappings(SERVICE_TOKEN, IDAM_TOKEN, STATE_CATEGORY_TO_EXCLUDE, CASE_TYPE_ID))
            .thenReturn(expectedResponse);

        ShellMappingResponse response = shellMappingService.loadMappings(CASE_TYPE_ID);

        assertThat(response).isSameAs(expectedResponse);
    }

    @Test
    void shouldReturnEmptyMapWhenCaseTypesIsNull() {
        Map<String, ShellMappingResponse> responseMap = shellMappingService.getShellMappings(null);

        assertThat(responseMap).isEmpty();
        verifyNoInteractions(shellMappingClient);
    }

    @Test
    void shouldReturnEmptyMapWhenCaseTypesIsEmpty() {
        Map<String, ShellMappingResponse> responseMap = shellMappingService.getShellMappings(List.of());

        assertThat(responseMap).isEmpty();
        verifyNoInteractions(shellMappingClient);
    }

    @Test
    void shouldReturnMappingsForEachCaseTypeWhenCaseTypesProvided() {
        stubAuthTokens();
        String firstCaseTypeId = "case-type-id-1";
        String secondCaseTypeId = "case-type-id-2";

        ShellMappingResponse firstResponse = new ShellMappingResponse("shell-case-type-1", List.of(), List.of());
        ShellMappingResponse secondResponse = new ShellMappingResponse("shell-case-type-2", List.of(), List.of());

        when(shellMappingClient.getShellMappings(
            SERVICE_TOKEN,
            IDAM_TOKEN,
            STATE_CATEGORY_TO_EXCLUDE,
            firstCaseTypeId
        ))
            .thenReturn(firstResponse);
        when(shellMappingClient.getShellMappings(
            SERVICE_TOKEN,
            IDAM_TOKEN,
            STATE_CATEGORY_TO_EXCLUDE,
            secondCaseTypeId
        )).thenReturn(secondResponse);

        Map<String, ShellMappingResponse> responseMap =
            shellMappingService.getShellMappings(List.of(firstCaseTypeId, secondCaseTypeId));

        assertThat(responseMap)
            .hasSize(2)
            .containsEntry(firstCaseTypeId, firstResponse)
            .containsEntry(secondCaseTypeId, secondResponse);

        verify(shellMappingClient, times(1)).getShellMappings(
            SERVICE_TOKEN,
            IDAM_TOKEN,
            STATE_CATEGORY_TO_EXCLUDE,
            firstCaseTypeId
        );
        verify(shellMappingClient, times(1)).getShellMappings(
            SERVICE_TOKEN,
            IDAM_TOKEN,
            STATE_CATEGORY_TO_EXCLUDE,
            secondCaseTypeId
        );
    }

    @Test
    void shouldWrapClientFailure() {
        stubAuthTokens();
        FeignException serviceUnavailable = mock(FeignException.class);
        when(shellMappingClient.getShellMappings(
            SERVICE_TOKEN,
            IDAM_TOKEN,
            STATE_CATEGORY_TO_EXCLUDE,
            CASE_TYPE_ID
        )).thenThrow(serviceUnavailable);

        assertThatThrownBy(() -> shellMappingService.loadMappings(CASE_TYPE_ID))
            .isInstanceOf(ShellCaseException.class)
            .hasMessage("Failed to retrieve mappings for case type " + CASE_TYPE_ID)
            .hasCause(serviceUnavailable);
    }

    @Test
    void shouldFailWhenClientReturnsNullResponse() {
        stubAuthTokens();
        when(shellMappingClient.getShellMappings(
            SERVICE_TOKEN,
            IDAM_TOKEN,
            STATE_CATEGORY_TO_EXCLUDE,
            CASE_TYPE_ID
        )).thenReturn(null);

        assertThatThrownBy(() -> shellMappingService.loadMappings(CASE_TYPE_ID))
            .isInstanceOf(ShellCaseException.class)
            .hasMessage("Shell mapping response was null for case type " + CASE_TYPE_ID);
    }

    @Test
    void shouldFailWhenMappedShellCaseTypeHasNoMappingsList() {
        stubAuthTokens();
        when(shellMappingClient.getShellMappings(
            SERVICE_TOKEN,
            IDAM_TOKEN,
            STATE_CATEGORY_TO_EXCLUDE,
            CASE_TYPE_ID
        )).thenReturn(new ShellMappingResponse("shell-case-type-id", List.of(), null));

        assertThatThrownBy(() -> shellMappingService.loadMappings(CASE_TYPE_ID))
            .isInstanceOf(ShellCaseException.class)
            .hasMessage("Shell mapping response was invalid for case type " + CASE_TYPE_ID);
    }

    @Test
    void shouldReturnCachedResponseAndNotCallClientAgainForSameCaseType() {
        stubAuthTokens();

        ShellMappingResponse expectedResponse =
            new ShellMappingResponse("shell-case-type-id", List.of(), List.of());

        when(shellMappingClient.getShellMappings(
            SERVICE_TOKEN,
            IDAM_TOKEN,
            STATE_CATEGORY_TO_EXCLUDE,
            CASE_TYPE_ID
        ))
            .thenReturn(expectedResponse);

        ShellMappingResponse firstResponse =
            shellMappingService.loadMappings(CASE_TYPE_ID);

        ShellMappingResponse secondResponse =
            shellMappingService.loadMappings(CASE_TYPE_ID);

        assertThat(firstResponse).isSameAs(expectedResponse);
        assertThat(secondResponse).isSameAs(expectedResponse);

        verify(shellMappingClient, times(1))
            .getShellMappings(SERVICE_TOKEN, IDAM_TOKEN, STATE_CATEGORY_TO_EXCLUDE, CASE_TYPE_ID);
    }

    @Test
    void shouldCallClientAgainForDifferentCaseType() {
        stubAuthTokens();

        String firstCaseTypeId = "case-type-id-1";
        String secondCaseTypeId = "case-type-id-2";

        ShellMappingResponse firstResponse =
            new ShellMappingResponse("shell-case-type-1", List.of(), List.of());
        ShellMappingResponse secondResponse =
            new ShellMappingResponse("shell-case-type-2", List.of(), List.of());

        when(shellMappingClient.getShellMappings(
            SERVICE_TOKEN,
            IDAM_TOKEN,
            STATE_CATEGORY_TO_EXCLUDE,
            firstCaseTypeId
        ))
            .thenReturn(firstResponse);

        when(shellMappingClient.getShellMappings(
            SERVICE_TOKEN,
            IDAM_TOKEN,
            STATE_CATEGORY_TO_EXCLUDE,
            secondCaseTypeId
        ))
            .thenReturn(secondResponse);

        ShellMappingResponse result1 =
            shellMappingService.loadMappings(firstCaseTypeId);

        ShellMappingResponse result2 =
            shellMappingService.loadMappings(secondCaseTypeId);

        assertThat(result1).isSameAs(firstResponse);
        assertThat(result2).isSameAs(secondResponse);

        verify(shellMappingClient, times(1))
            .getShellMappings(SERVICE_TOKEN, IDAM_TOKEN, STATE_CATEGORY_TO_EXCLUDE, firstCaseTypeId);

        verify(shellMappingClient, times(1))
            .getShellMappings(SERVICE_TOKEN, IDAM_TOKEN, STATE_CATEGORY_TO_EXCLUDE, secondCaseTypeId);
    }

    @Test
    void shouldCacheEmptyResponseWhenShellMappingDoesNotExist() {
        stubAuthTokens();

        ShellMappingResponse missingMapping = new ShellMappingResponse(null, List.of(), null);
        when(shellMappingClient.getShellMappings(
            SERVICE_TOKEN,
            IDAM_TOKEN,
            STATE_CATEGORY_TO_EXCLUDE,
            CASE_TYPE_ID
        ))
            .thenReturn(missingMapping);

        ShellMappingResponse firstResponse =
            shellMappingService.loadMappings(CASE_TYPE_ID);

        ShellMappingResponse secondResponse =
            shellMappingService.loadMappings(CASE_TYPE_ID);

        assertThat(firstResponse).isSameAs(missingMapping);
        assertThat(secondResponse).isSameAs(firstResponse);

        verify(shellMappingClient, times(1))
            .getShellMappings(SERVICE_TOKEN, IDAM_TOKEN, STATE_CATEGORY_TO_EXCLUDE, CASE_TYPE_ID);
    }

    private void stubAuthTokens() {
        when(securityUtil.getServiceAuthorization()).thenReturn(SERVICE_TOKEN);
        when(securityUtil.getIdamClientToken()).thenReturn(IDAM_TOKEN);
    }
}

