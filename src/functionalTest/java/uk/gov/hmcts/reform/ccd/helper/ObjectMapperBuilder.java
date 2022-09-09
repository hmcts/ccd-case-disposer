package uk.gov.hmcts.reform.ccd.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.exception.CaseDisposerFunctionalTestException;

import java.io.IOException;
import java.util.Map;

import static org.springframework.util.ResourceUtils.getFile;

public class ObjectMapperBuilder {

    private ObjectMapperBuilder() {
    }

    private static final ObjectMapperBuilder.MapTypeReference MAP_TYPE = new ObjectMapperBuilder.MapTypeReference();

    public static Map<String, Object> caseData(final String resourcePath) {
        try {
            return getObjectMapper().readValue(getFile(resourcePath), MAP_TYPE);
        } catch (final IOException ioException) {
            throw new CaseDisposerFunctionalTestException("Unable to create ObjectMapperBuilder", ioException);
        }
    }

    private static ObjectMapper getObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    private static class MapTypeReference extends TypeReference<Map<String, Object>> {
    }
}
