package io.fabric8.launcher.core.impl.catalog;

import java.util.Collections;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ScriptingRhoarBoosterPredicateTest {

    @Test
    public void should_evaluate_to_true() {
        ScriptingRhoarBoosterPredicate predicate = new ScriptingRhoarBoosterPredicate("booster != null");
        assertThat(predicate.test(mock(RhoarBooster.class))).isTrue();
    }

    @Test
    public void should_evaluate_to_false() {
        ScriptingRhoarBoosterPredicate predicate = new ScriptingRhoarBoosterPredicate("booster == null");
        assertThat(predicate.test(mock(RhoarBooster.class))).isFalse();
    }

    @Test
    public void should_evaluate_metadata_to_true() {
        ScriptingRhoarBoosterPredicate predicate = new ScriptingRhoarBoosterPredicate("booster.metadata.istio");
        RhoarBooster booster = mock(RhoarBooster.class);
        when(booster.getMetadata()).thenReturn(Collections.singletonMap("istio", "true"));
        assertThat(predicate.test(booster)).isTrue();
    }

    @Test
    public void should_evaluate_metadata_to_false() {
        ScriptingRhoarBoosterPredicate predicate = new ScriptingRhoarBoosterPredicate("booster.metadata.istio");
        RhoarBooster booster = mock(RhoarBooster.class);
        when(booster.getMetadata()).thenReturn(Collections.singletonMap("istio", "true"));
        assertThat(predicate.test(booster)).isTrue();
    }

}