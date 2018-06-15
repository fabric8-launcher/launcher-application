package io.fabric8.launcher.web.endpoints.inputs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by hrishin on 6/15/18.
 */
public class LaunchProjectileInputTest {

    private Validator validator;

    private  JSONObject launchProjectInputs;

    @Before
    public void init() {
        initializeValidator();
        initializeLaunchInputs();
    }

    @Test
    public void shouldNotViolateProjectNameConstraints() throws IOException {
        // GIVEN
        launchProjectInputs.put("projectName", "Test-_123");

        // WHEN
        Set<ConstraintViolation<LaunchProjectileInput>> violations = checkConstraintViolations(launchProjectInputs);

        // THEN
        assertTrue(violations.isEmpty());
    }

    @Test
    public void projectNameShouldStartWithAlphabeticCharacters() throws IOException {
        // GIVEN
        launchProjectInputs.put("projectName", "123Test");

        // WHEN
        Set<ConstraintViolation<LaunchProjectileInput>> violations = checkConstraintViolations(launchProjectInputs);

        // THEN
        assertFalse(violations.isEmpty());
        assertEquals(hasMessage(violations, LaunchProjectileInput.PROJECT_NAME_VALIDATION_MESSAGE), true);
    }

    @Test
    public void projectNameShouldEndWithAlphanumericCharacters() throws IOException {
        // GIVEN
        launchProjectInputs.put("projectName", "123Test**");

        // WHEN
        Set<ConstraintViolation<LaunchProjectileInput>> violations = checkConstraintViolations(launchProjectInputs);

        // THEN
        assertFalse(violations.isEmpty());
        assertEquals(hasMessage(violations, LaunchProjectileInput.PROJECT_NAME_VALIDATION_MESSAGE), true);
    }

    private void initializeLaunchInputs() {
        launchProjectInputs = new JSONObject();
        launchProjectInputs.put("runtime", "vertx")
                .put("mission", "http-vertex")
                .put("gitRepository", "http-vertex");
    }

    private void initializeValidator() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        this.validator = validatorFactory.getValidator();
    }

    private Set<ConstraintViolation<LaunchProjectileInput>> checkConstraintViolations(JSONObject jsonObject) throws IOException {
        LaunchProjectileInput projectInput = new ObjectMapper()
                .readValue(jsonObject.toString(), LaunchProjectileInput.class);
        return this.validator.validate(projectInput);
    }

    private boolean hasMessage(Set<ConstraintViolation<LaunchProjectileInput>> violations, String message) {
        return violations
                .stream()
                .anyMatch(v -> v.getMessage().equalsIgnoreCase(message));
    }
}
