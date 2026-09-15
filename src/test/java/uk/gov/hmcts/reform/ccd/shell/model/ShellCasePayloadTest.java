package uk.gov.hmcts.reform.ccd.shell.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import static org.assertj.core.api.Assertions.assertThat;

class ShellCasePayloadTest {

    @Test
    void shouldSerializeCcdCreateCasePayload() throws Exception {
        ObjectNode data = JsonNodeFactory.instance.objectNode()
            .put("original_case_reference", "1234567890123456")
            .put("mapped_field", "mapped value");
        ShellCasePayload payload = new ShellCasePayload(data, "event-token");

        JsonNode json = JsonMapper.builder().build().valueToTree(payload);

        assertThat(json.get("data")).isEqualTo(data);
        assertThat(json.get("event_token").asString()).isEqualTo("event-token");
        assertThat(json.get("event").get("id").asString()).isEqualTo("createCase");
        assertThat(json.get("event").get("summary").asString()).isEqualTo("Create case");
        assertThat(json.get("event").get("description").asString()).isEqualTo("Initial case creation");
        assertThat(json.get("ignore_warning").asBoolean()).isFalse();
    }
}
