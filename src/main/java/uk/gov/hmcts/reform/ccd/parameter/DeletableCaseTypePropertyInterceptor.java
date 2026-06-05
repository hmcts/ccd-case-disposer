package uk.gov.hmcts.reform.ccd.parameter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.exception.DuplicateCaseTypeException;

import java.util.List;

@Slf4j
@Configuration
public class DeletableCaseTypePropertyInterceptor {

    private final ParameterResolver parameterResolver;

    public DeletableCaseTypePropertyInterceptor(ParameterResolver parameterResolver) {
        this.parameterResolver = parameterResolver;
    }

    @PostConstruct
    public void init() throws DuplicateCaseTypeException {
        final List<String> deletableCaseTypes = parameterResolver.getDeletableCaseTypes();
        final List<String> deletableCaseTypesSimulation = parameterResolver.getDeletableCaseTypesSimulation();

        final List<String> duplicateCaseTypes = deletableCaseTypes.stream()
                .filter(deletableCaseTypesSimulation::contains)
                .filter(caseTypes -> !caseTypes.isEmpty())
                .toList();

        if (!duplicateCaseTypes.isEmpty()) {
            log.error("Found duplicate deletable case type in "
                    + "application.yaml: " + duplicateCaseTypes);
            throw new DuplicateCaseTypeException("Found duplicate deletable case type in "
                    + "application.yaml: " + duplicateCaseTypes);
        }
    }
}

