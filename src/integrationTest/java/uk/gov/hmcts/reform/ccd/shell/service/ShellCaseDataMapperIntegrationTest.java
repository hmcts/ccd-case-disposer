package uk.gov.hmcts.reform.ccd.shell.service;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.ccd.shell.model.ShellCaseFieldMapping;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ShellCaseDataMapper.class)
class ShellCaseDataMapperIntegrationTest {

    @Inject
    private ShellCaseDataMapper shellCaseDataMapper;

    @Test
    void shouldWireMapperAndMapCaseData() {
        ObjectNode caseData = JsonNodeFactory.instance.objectNode();
        caseData.putObject("applicant").put("name", "Jane Smith");

        ObjectNode result = shellCaseDataMapper.map(
            caseData,
            List.of(new ShellCaseFieldMapping("applicant.name", "party.fullName"))
        );

        assertThat(result.get("party").get("fullName").stringValue()).isEqualTo("Jane Smith");
    }
}
