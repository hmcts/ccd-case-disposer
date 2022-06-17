package uk.gov.hmcts.reform.ccd.service.remote;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.data.am.RoleAssignmentsDeletePostRequest;
import uk.gov.hmcts.reform.ccd.exception.RoleAssignmentDeletionException;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.util.log.RoleDeletionRecordHolder;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.util.RestConstants.DELETE_ROLE_PATH;

@DisplayName("dispose case role assignments")
@ExtendWith(MockitoExtension.class)
class DisposeRoleAssignmentsRemoteOperationTest {

    @Mock
    private RestClientBuilder restClientBuilder;

    @Mock
    private RoleDeletionRecordHolder roleDeletionRecordHolder;

    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private DisposeRoleAssignmentsRemoteOperation disposeRoleAssignmentsRemoteOperation;

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("should post role assignment delete remote dispose request")
    void shouldPostRoleAssignmentsDeleteRemoteDisposeRequest() {

        final Gson gson = new Gson();
        final Response response = mock(Response.class);

        final String caseRef = "1234567890123456";
        final RoleAssignmentsDeletePostRequest roleAssignmentsDeleteRequest =
                new RoleAssignmentsDeletePostRequest(caseRef);

        doReturn("http://localhost").when(parameterResolver).getRoleAssignmentsHost();
        when(response.getStatus()).thenReturn(200);

        final String requestBody = gson.toJson(roleAssignmentsDeleteRequest);

        when(restClientBuilder.postRequestWithAllHeaders("http://localhost", DELETE_ROLE_PATH, requestBody)).thenReturn(response);

        disposeRoleAssignmentsRemoteOperation.postRoleAssignmentsDelete(caseRef);

        verify(roleDeletionRecordHolder, times(1)).setCaseRolesDeletionResults("1234567890123456",
                200);
        verify(restClientBuilder, times(1)).postRequestWithAllHeaders("http://localhost",
                DELETE_ROLE_PATH,
                requestBody);
    }

    @Test
    void shouldThrowExceptionWhenRequestInvalid() {
        try {
            final String caseRef = "1234567890123456";
            final String jsonRequest = new Gson().toJson(new RoleAssignmentsDeletePostRequest("1234567890123456"));
            doReturn("http://localhost").when(parameterResolver).getRoleAssignmentsHost();

            doThrow(new RoleAssignmentDeletionException(caseRef))
                    .when(restClientBuilder)
                    .postRequestWithAllHeaders("http://localhost", DELETE_ROLE_PATH, jsonRequest);

            disposeRoleAssignmentsRemoteOperation.postRoleAssignmentsDelete(caseRef);

            fail("The method should have thrown DocumentDeletionException when request is invalid");
        } catch (final RoleAssignmentDeletionException roleAssignmentDeletionException) {
            assertThat(roleAssignmentDeletionException.getMessage())
                    .isEqualTo("Error deleting role assignments for case : 1234567890123456");
        }
    }
}
