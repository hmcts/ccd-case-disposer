package uk.gov.hmcts.reform.ccd.shell.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.ccd.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.model.ShellCaseFieldMapping;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
class ShellCaseDataMapperTest {

    private ShellCaseDataMapper shellCaseDataMapper;

    @BeforeEach
    void setUp() {
        shellCaseDataMapper = new ShellCaseDataMapper();
    }

    @Test
    void shouldMapTopLevelAndNestedFields() {
        ObjectNode caseData = JsonNodeFactory.instance.objectNode();
        caseData.put("caseNumber", "1234567890123456");
        caseData.putObject("applicant")
            .put("firstName", "John")
            .put("lastName", "Smith");

        ObjectNode result = shellCaseDataMapper.map(caseData, List.of(
            mapping("caseNumber", "legacyReference"),
            mapping("applicant.firstName", "party.name.first"),
            mapping("applicant.lastName", "party.name.last")
        ));

        assertThat(result.get("legacyReference").asString()).isEqualTo("1234567890123456");
        assertThat(result.get("party").get("name").get("first").asString()).isEqualTo("John");
        assertThat(result.get("party").get("name").get("last").asString()).isEqualTo("Smith");
    }

    @Test
    void shouldReturnEmptyObjectWhenNoMappingsAreProvided() {
        ObjectNode caseData = JsonNodeFactory.instance.objectNode();
        caseData.put("caseNumber", "1234567890123456");

        ObjectNode result = shellCaseDataMapper.map(caseData, List.of());

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void shouldOmitMappingWhenSourceFieldIsAbsent() {
        ObjectNode caseData = JsonNodeFactory.instance.objectNode();
        caseData.put("existing", "value");

        ObjectNode result = shellCaseDataMapper.map(caseData, List.of(
            mapping("missing.field", "shell.missing"),
            mapping("existing", "shell.existing")
        ));

        assertThat(result.get("shell").has("missing")).isFalse();
        assertThat(result.get("shell").get("existing").asString()).isEqualTo("value");
    }

    @Test
    void shouldPreserveExplicitNullSourceValue() {
        ObjectNode caseData = JsonNodeFactory.instance.objectNode();
        caseData.putNull("value");

        ObjectNode result = shellCaseDataMapper.map(caseData, List.of(mapping("value", "mappedValue")));

        assertThat(result.has("mappedValue")).isTrue();
        assertThat(result.get("mappedValue").isNull()).isTrue();
    }

    @Test
    void shouldDeepCopyMappedObjectsAndArrays() {
        ObjectNode caseData = JsonNodeFactory.instance.objectNode();
        ObjectNode details = caseData.putObject("details");
        ArrayNode documents = details.putArray("documents");
        documents.addObject().put("name", "original.pdf");

        ObjectNode result = shellCaseDataMapper.map(caseData, List.of(mapping("details", "archivedDetails")));
        details.put("status", "changed");
        documents.addObject().put("name", "updated.pdf");

        ObjectNode archivedDetails = (ObjectNode) result.get("archivedDetails");
        assertThat(archivedDetails.has("status")).isFalse();
        assertThat(archivedDetails.get("documents")).hasSize(1);
        assertThat(archivedDetails.get("documents").get(0).get("name").asString()).isEqualTo("original.pdf");
    }

    @Test
    void shouldFailWhenSourcePathTraversesNonObjectValue() {
        ObjectNode caseData = JsonNodeFactory.instance.objectNode();
        caseData.put("applicant", "not-an-object");

        assertThatThrownBy(() -> shellCaseDataMapper.map(
            caseData,
            List.of(mapping("applicant.name", "party.name"))
        ))
            .isInstanceOf(ShellCaseException.class)
            .hasMessageContaining("applicant.name")
            .hasMessageContaining("is not an object");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", ".name", "name.", "name..first"})
    void shouldRejectInvalidSourcePath(String sourcePath) {
        ObjectNode caseData = JsonNodeFactory.instance.objectNode();

        assertThatThrownBy(() -> shellCaseDataMapper.map(
            caseData,
            List.of(mapping(sourcePath, "validTarget"))
        ))
            .isInstanceOf(ShellCaseException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", ".name", "name.", "name..first"})
    void shouldRejectInvalidTargetPath(String targetPath) {
        ObjectNode caseData = JsonNodeFactory.instance.objectNode();

        assertThatThrownBy(() -> shellCaseDataMapper.map(
            caseData,
            List.of(mapping("validSource", targetPath))
        ))
            .isInstanceOf(ShellCaseException.class);
    }

    @Test
    void shouldRejectDuplicateTargetPaths() {
        ObjectNode caseData = JsonNodeFactory.instance.objectNode();
        caseData.put("first", "one");
        caseData.put("second", "two");

        assertThatThrownBy(() -> shellCaseDataMapper.map(caseData, List.of(
            mapping("first", "duplicate"),
            mapping("second", "duplicate")
        )))
            .isInstanceOf(ShellCaseException.class)
            .hasMessage("Conflicting shell case target mappings: 'duplicate' and 'duplicate'");
    }

    @Test
    void shouldRejectTargetPathThatIsDescendantOfExistingTarget() {
        ObjectNode caseData = JsonNodeFactory.instance.objectNode();
        caseData.put("first", "one");
        caseData.put("second", "two");

        assertThatThrownBy(() -> shellCaseDataMapper.map(caseData, List.of(
            mapping("first", "party"),
            mapping("second", "party.name")
        )))
            .isInstanceOf(ShellCaseException.class)
            .hasMessage("Conflicting shell case target mappings: 'party' and 'party.name'");
    }

    @Test
    void shouldRejectTargetPathThatIsAncestorOfExistingTarget() {
        ObjectNode caseData = JsonNodeFactory.instance.objectNode();
        caseData.put("first", "one");
        caseData.put("second", "two");

        assertThatThrownBy(() -> shellCaseDataMapper.map(caseData, List.of(
            mapping("first", "party.name"),
            mapping("second", "party")
        )))
            .isInstanceOf(ShellCaseException.class)
            .hasMessage("Conflicting shell case target mappings: 'party.name' and 'party'");
    }

    private ShellCaseFieldMapping mapping(String sourcePath, String targetPath) {
        return new ShellCaseFieldMapping(sourcePath, targetPath);
    }
}
