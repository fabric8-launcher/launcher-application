package io.fabric8.launcher.base.test.hoverfly;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createHoverflyProxy;

public class LauncherPerTestHoverflyRule implements TestRule {

    private final String destination;

    public LauncherPerTestHoverflyRule(String destination) {
        this.destination = destination;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        final HoverflyRule hoverflyRule = createHoverflyProxy(getSimulationFileName(description), destination);
        return hoverflyRule.apply(base, description);
    }

    private String getSimulationFileName(Description description) {
        return description.getTestClass().getSimpleName().toLowerCase() + "/" + description.getMethodName().toLowerCase() + ".json";
    }

}

