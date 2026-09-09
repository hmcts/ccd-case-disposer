package uk.gov.hmcts.reform.ccd.shell.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.ccd.exception.ShellCaseException;
import uk.gov.hmcts.reform.ccd.shell.model.ShellCaseFieldMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Copy json data from original case to a new object based on the shell mappings.
 * Current logic, assumptions and behaviour is documented here:
 * https://tools.hmcts.net/confluence/spaces/LAU/pages/1973309155/05+Shell+Cases+LLD#id-05ShellCasesLLD-Datamappingdecisions
 * Please update the above document if any logic changes
 */
@Service
@RequiredArgsConstructor
public class ShellCaseDataMapper {

    public ObjectNode map(JsonNode caseJsonData, List<ShellCaseFieldMapping> mappings) {

        ObjectNode shellData = JsonNodeFactory.instance.objectNode();
        Set<String> targetPaths = new HashSet<>();

        for (ShellCaseFieldMapping mapping : mappings) {
            String sourcePath = mapping.getOriginatingCaseFieldName();
            String targetPath = mapping.getShellCaseFieldName();

            validateMapping(sourcePath, targetPath, targetPaths);

            JsonNode value = readValue(caseJsonData, sourcePath);

            // An absent optional source field is simply omitted.
            if (value != null) {
                writeValue(shellData, targetPath, value.deepCopy());
            }
        }

        return shellData;
    }

    private JsonNode readValue(JsonNode root, String path) {
        JsonNode current = root;

        for (String segment: splitPath(path)) {
            if (!current.isObject()) {
                throw new ShellCaseException(
                    "Cannot read shell mapping source path '%s': '%s' is not an object"
                        .formatted(path, segment)
                );
            }

            current = current.get(segment);

            if (current == null) {
                return null;
            }
        }

        return current;
    }

    private void writeValue(ObjectNode root, String path, JsonNode value) {
        String[] segments = splitPath(path);
        ObjectNode current = root;

        for (int index = 0; index < segments.length - 1; index++) {
            String segment = segments[index];
            JsonNode existing = current.get(segment);

            if (existing == null) {
                ObjectNode child = JsonNodeFactory.instance.objectNode();
                current.set(segment, child);
                current = child;
            } else if (existing.isObject()) {
                current = (ObjectNode) existing;
            } else {
                throw new ShellCaseException(
                    "Cannot write shell mapping target path '%s': "
                        + "'%s' is already a non-object value"
                        .formatted(path, segment)
                );
            }
        }

        current.set(segments[segments.length - 1], value);
    }

    private String[] splitPath(String path) {
        if (path == null || path.isBlank()) {
            throw new ShellCaseException(
                "Shell case mapping path must not be blank"
            );
        }

        String[] segments = path.split("\\.", -1);

        for (String segment : segments) {
            if (segment.isBlank()) {
                throw new ShellCaseException(
                    "Invalid shell case mapping path: " + path
                );
            }
        }

        return segments;
    }

    private void validateMapping(String sourcePath, String targetPath, Set<String> targetPaths) {
        splitPath(sourcePath);
        splitPath(targetPath);

        for (String existing : targetPaths) {
            if (existing.equals(targetPath)
                || existing.startsWith(targetPath + ".")
                || targetPath.startsWith(existing + ".")) {
                throw new ShellCaseException(
                    "Conflicting shell case target mappings: '%s' and '%s'"
                        .formatted(existing, targetPath)
                );
            }
        }

        targetPaths.add(targetPath);
    }

}
