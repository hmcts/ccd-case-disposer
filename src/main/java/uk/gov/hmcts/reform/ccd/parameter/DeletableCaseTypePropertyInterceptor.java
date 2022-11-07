package uk.gov.hmcts.reform.ccd.parameter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.exception.DuplicateCaseTypeException;

import java.util.List;
import javax.annotation.PostConstruct;

import static java.util.stream.Collectors.toList;

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
                .filter(deletableCaseType -> deletableCaseTypesSimulation.contains(deletableCaseType))
                .filter(caseTypes -> !caseTypes.isEmpty())
                .collect(toList());

        if (!duplicateCaseTypes.isEmpty()) {
            log.error("Found duplicate deletable case type in "
                    + "application.yaml: " + duplicateCaseTypes);
            throw new DuplicateCaseTypeException("Found duplicate deletable case type in "
                    + "application.yaml: " + duplicateCaseTypes);
        }
    }
}

