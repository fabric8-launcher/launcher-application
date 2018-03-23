package io.fabric8.launcher.base.test.hoverfly;

import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static io.specto.hoverfly.junit.core.SimulationSource.defaultPath;

public class LauncherPerTestHoverflyRule implements TestRule {

    private final HoverflyRule hoverflyRule;

    public LauncherPerTestHoverflyRule(HoverflyRule hoverflyRule) {
        this.hoverflyRule = hoverflyRule;
    }


    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (HoverflyMode.CAPTURE == hoverflyRule.getHoverflyMode()) {
                    hoverflyRule.capture("captured/" + getSimulationFileName(description));
                } else {
                    hoverflyRule.simulate(defaultPath(getSimulationFileName(description)));
                }
                base.evaluate();
            }
        };
    }

    private String getSimulationFileName(Description description) {
        return description.getTestClass().getSimpleName().toLowerCase() + "/" + description.getMethodName().toLowerCase() + ".json";
    }

}

