package io.fabric8.launcher.base.http;

import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.Request;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class RequestsTest {

    @Test
    public void execute_should_be_successfull() {
        final Request request = new Request.Builder().url("https://www.github.com").build();
        final boolean isSuccessful = Requests.execute(request);
        assertThat(isSuccessful).isTrue();
    }

    @Test
    public void readJson_should_be_successfull() {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts/1").build();
        final Optional<JsonNode> jsonContent = Requests.executeAndParseJson(request, Function.identity());
        assertThat(jsonContent).isPresent();
        assertThat(jsonContent.get().get("userId").asInt()).isEqualTo(1);
    }

}
