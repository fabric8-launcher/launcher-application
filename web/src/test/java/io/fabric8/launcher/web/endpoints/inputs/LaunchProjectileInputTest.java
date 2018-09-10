package io.fabric8.launcher.web.endpoints.inputs;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import io.fabric8.launcher.booster.catalog.rhoar.Mission;
import io.fabric8.launcher.booster.catalog.rhoar.Runtime;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by hrishin on 6/15/18.
 */
public class LaunchProjectileInputTest {

    private Validator validator;

    private LaunchProjectileInput launchProjectInput;

    @Before
    public void init() {
        initializeValidator();
        initializeLaunchInputs();
    }

    @Test
    public void shouldNotViolateProjectNameConstraints() {
        // GIVEN
        launchProjectInput.setProjectName("Test-_123");

        // WHEN
        Set<ConstraintViolation<LaunchProjectileInput>> violations = validator.validate(launchProjectInput);

        // THEN
        assertThat(violations).isEmpty();
    }

    @Test
    public void projectNameShouldStartWithAlphabeticCharacters() {
        // GIVEN
        launchProjectInput.setProjectName("123Test");

        // WHEN
        Set<ConstraintViolation<LaunchProjectileInput>> violations = validator.validate(launchProjectInput);

        // THEN
        assertThat(violations).isNotEmpty();
        assertThat(hasMessage(violations, OpenShiftService.PROJECT_NAME_VALIDATION_MESSAGE)).isTrue();
    }

    @Test
    public void projectNameShouldEndWithAlphanumericCharacters() {
        // GIVEN
        launchProjectInput.setProjectName("123Test**");

        // WHEN
        Set<ConstraintViolation<LaunchProjectileInput>> violations = validator.validate(launchProjectInput);

        // THEN
        assertThat(violations).isNotEmpty();
        assertThat(hasMessage(violations, OpenShiftService.PROJECT_NAME_VALIDATION_MESSAGE)).isTrue();
    }

    private void initializeValidator() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        this.validator = validatorFactory.getValidator();
    }

    private void initializeLaunchInputs() {
        launchProjectInput = new LaunchProjectileInput();
        launchProjectInput.setMission(new Mission("rest-http"));
        launchProjectInput.setRuntime(new Runtime("vert.x"));
        launchProjectInput.setGitRepository("foo");
    }

    private boolean hasMessage(Set<ConstraintViolation<LaunchProjectileInput>> violations, String message) {
        return violations
                .stream()
                .anyMatch(v -> v.getMessage().equalsIgnoreCase(message));
    }
}
