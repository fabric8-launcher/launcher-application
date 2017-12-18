package io.fabric8.launcher.web.forge.util;

import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class ResultsTest {

    @Test
    public void findReturnMapWithCompositeResultShouldBeSame() {
        Map<String, String> map = Collections.singletonMap("key", "value");
        Result result = Results.aggregate(Collections.singletonList(Results.success("Success", map)));
        Map<String, String> returnMap = io.fabric8.launcher.web.forge.util.Results.getEntityAsMap(result);
        assertThat(returnMap).isSameAs(map);
    }

    @Test
    public void findReturnMapWithSingleResultShouldBeSame() {
        Map<String, String> map = Collections.singletonMap("key", "value");
        Result result = Results.success("Success", map);
        Map<String, String> returnMap = io.fabric8.launcher.web.forge.util.Results.getEntityAsMap(result);
        assertThat(returnMap).isEmpty();
    }

    @Test
    public void findReturnMapWithNullResultShouldBeSame() {
        Map<String, String> returnMap = io.fabric8.launcher.web.forge.util.Results.getEntityAsMap(null);
        assertThat(returnMap).isEmpty();
    }

    @Test
    public void getEntityWithSingleResultReturnsEntity() {
        Map<String, String> map = Collections.singletonMap("key", "value");
        Result result = Results.success("Success", map);
        Object returnMap = io.fabric8.launcher.web.forge.util.Results.getEntity(result);
        assertThat(returnMap).isSameAs(map);
    }

    @Test
    public void getEntityWithCompositeResultReturnsList() {
        Result result = Results.aggregate(Arrays.asList(Results.success(), Results.fail()));
        Object ret = io.fabric8.launcher.web.forge.util.Results.getEntity(result);
        assertThat(ret).isInstanceOf(List.class);
        assertThat((List<String>) ret).hasSize(2);
    }

    @Test
    public void getEntityWithNullResultReturnsNull() {
        Object ret = io.fabric8.launcher.web.forge.util.Results.getEntity(null);
        assertThat(ret).isNull();
    }

    @Test
    public void getMessageWithSingleResultReturnsSingleMessage() {
        Result result = Results.success("Everything is fine");
        String message = io.fabric8.launcher.web.forge.util.Results.getMessage(result);
        assertThat(message).isEqualTo(result.getMessage());
    }

    @Test
    public void getMessageWithCompositeResultReturnsConcatenatedMessage() {
        Result result = Results.aggregate(Arrays.asList(Results.success("A"), Results.fail("B")));
        String message = io.fabric8.launcher.web.forge.util.Results.getMessage(result);
        assertThat(message).isEqualTo("A\nB");
    }

    @Test
    public void getMessageWithNullResultReturnsNull() {
        Object ret = io.fabric8.launcher.web.forge.util.Results.getMessage(null);
        assertThat(ret).isNull();
    }
}
