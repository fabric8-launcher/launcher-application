package io.fabric8.launcher.osio.projectiles.context;

import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by hshinde on 7/4/18.
 */
public class OsioImportProjectileContextTest {

    private Validator validator;

    private OsioImportProjectileContext launchProjectInput;

    @Before
    public void init() {
        initializeValidator();
        initializeLaunchInputs();
    }

    @Test
    public void shouldNotViolateProjectNameConstraints() {
        // GIVEN
        launchProjectInput.setProjectName("test-123");

        // WHEN
        Set<ConstraintViolation<OsioImportProjectileContext>> violations = validator.validate(launchProjectInput);

        // THEN
        assertThat(violations).isEmpty();
    }

    @Test
    public void projectNameShouldNotExceedMaxLength() {
        // GIVEN
        launchProjectInput.setProjectName("test-123test-123test-123test-123test-123test-123test-123");

        // WHEN
        Set<ConstraintViolation<OsioImportProjectileContext>> violations = validator.validate(launchProjectInput);

        // THEN
        assertThat(violations).isNotEmpty();
        assertThat(hasMessage(violations, OsioImportProjectileContext.PROJECT_NAME_VALIDATION_MESSAGE)).isTrue();
    }

    private void initializeValidator() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        this.validator = validatorFactory.getValidator();
    }

    private void initializeLaunchInputs() {
        launchProjectInput = new OsioImportProjectileContext();
        launchProjectInput.setGitRepository("foo");
        launchProjectInput.setSpaceId("foo");
        launchProjectInput.setPipelineId("foo");
    }

    private boolean hasMessage(Set<ConstraintViolation<OsioImportProjectileContext>> violations, String message) {
        return violations
                .stream()
                .anyMatch(v -> v.getMessage().equalsIgnoreCase(message));
    }

}