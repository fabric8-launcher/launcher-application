package io.fabric8.launcher.core.impl.catalog;

import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;

/**
 * This predicate takes a JavaScript expression and evaluates to true or false, using "booster" as the argument
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class ScriptingRhoarBoosterPredicate implements Predicate<RhoarBooster> {

    private final CompiledScript script;

    private static final Logger log = Logger.getLogger(ScriptingRhoarBoosterPredicate.class.getName());

    public ScriptingRhoarBoosterPredicate(String evalScript) {
        ScriptEngineManager manager = new ScriptEngineManager(getClass().getClassLoader());
        ScriptEngine engine = manager.getEngineByExtension("js");
        try {
            this.script = ((Compilable) engine).compile(evalScript);
        } catch (ScriptException e) {
            throw new IllegalArgumentException("script is invalid", e);
        }
    }

    @Override
    public boolean test(RhoarBooster rhoarBooster) {
        ScriptContext context = new SimpleScriptContext();
        context.setAttribute("booster", rhoarBooster, ScriptContext.ENGINE_SCOPE);
        Object result = Boolean.FALSE;
        try {
            result = script.eval(context);
        } catch (ScriptException e) {
            log.log(Level.WARNING, "Error while evaluating script", e);
        }
        return (result instanceof Boolean) ? ((Boolean) result).booleanValue() :
                Boolean.valueOf(String.valueOf(result));
    }
}
