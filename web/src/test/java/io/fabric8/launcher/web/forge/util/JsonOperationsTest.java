package io.fabric8.launcher.web.forge.util;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class JsonOperationsTest {

    @Test
    public void testExceptionToJsonWithException() {
        Throwable t = new Exception("Foo");
        JsonObject json = JsonOperations.exceptionToJson(t, 1);
        assertThat(json.getString("type")).isEqualTo(t.getClass().getName());
    }

    @Test(expected = NullPointerException.class)
    public void testExceptionToJsonWithNullException() {
        JsonOperations.exceptionToJson(null, 1);
    }

    @Test
    public void testUnwrapJsonObjects() {
        JsonObject jsonObject = Json.createObjectBuilder().add("key", "value").build();
        Object o = JsonOperations.unwrapJsonObjects(jsonObject);
        assertThat(o).isInstanceOf(String.class);
        assertThat(o.toString()).isEqualTo("{\"key\":\"value\"}");
    }


}
