package uk.gov.hmcts.reform.ccd.shell.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "features.shell-case")
@Getter
@Setter
public class ShellCaseProperties {
    private boolean enabled;
}
