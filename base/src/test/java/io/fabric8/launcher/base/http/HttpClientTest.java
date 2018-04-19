package io.fabric8.launcher.base.http;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import okhttp3.Request;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyEnvironment.createDefaultHoverflyEnvironment;
import static io.fabric8.launcher.base.test.hoverfly.LauncherHoverflyRuleConfigurer.createMultiTestHoverflyProxy;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class HttpClientTest {

    private static final HoverflyRule HOVERFLY_RULE = createMultiTestHoverflyProxy("jsonplaceholder.typicode.com|api.github.com");

    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain// After recording on a real environment against a real service,
            // You should adapt the Hoverfly descriptors (.json) to make them work in simulation mode with the mock environment.
            .outerRule(createDefaultHoverflyEnvironment(HOVERFLY_RULE))
            .around(HOVERFLY_RULE);

    @Rule
    public LauncherPerTestHoverflyRule hoverflyPerTestRule = new LauncherPerTestHoverflyRule(HOVERFLY_RULE);


    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private HttpClient httpClient = new HttpClient(null);

    @Test
    public void should_execute_correctly() {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com").build();
        assertThat(httpClient.execute(request)).isTrue();
    }

    @Test
    public void should_execute_and_fail_correctly() {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/not-found").build();
        assertThat(httpClient.execute(request)).isFalse();
    }

    @Test
    public void should_read_json_correctly() {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts/1").build();
        final Optional<JsonNode> jsonContent = httpClient.executeAndParseJson(request, Function.identity());
        softly.assertThat(jsonContent).isPresent()
                .get()
                .hasToString("{\"userId\":1,\"id\":1,\"title\":\"sunt aut facere repellat provident occaecati excepturi optio reprehenderit\",\"body\":\"quia et suscipit\\nsuscipit recusandae consequuntur expedita et cum\\nreprehenderit molestiae ut ut quas totam\\nnostrum rerum est autem sunt rem eveniet architecto\"}");
    }

    @Test
    public void should_read_json_correctly_async() throws Exception {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts/1").build();
        final Optional<JsonNode> jsonContent = httpClient.executeAndParseJsonAsync(request, Function.identity()).get();
        softly.assertThat(jsonContent).isPresent()
                .get()
                .hasToString("{\"userId\":1,\"id\":1,\"title\":\"sunt aut facere repellat provident occaecati excepturi optio reprehenderit\",\"body\":\"quia et suscipit\\nsuscipit recusandae consequuntur expedita et cum\\nreprehenderit molestiae ut ut quas totam\\nnostrum rerum est autem sunt rem eveniet architecto\"}");
    }

    @Test
    public void should_return_empty_when_not_found_using_parse_json_correctly() {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts/not-found").build();
        final Optional<JsonNode> jsonContent = httpClient.executeAndParseJson(request, Function.identity());
        softly.assertThat(jsonContent).isEmpty();
    }

    @Test
    public void should_return_empty_when_not_found_using_parse_json_correctly_async() throws Exception {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts/not-found").build();
        final Optional<JsonNode> jsonContent = httpClient.executeAndParseJsonAsync(request, Function.identity()).get();
        softly.assertThat(jsonContent).isEmpty();
    }


    @Test
    public void should_return_empty_when_parse_json_returns_null_correctly() {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts/1").build();
        final Optional<JsonNode> jsonContent = httpClient.executeAndParseJson(request, n -> null);
        softly.assertThat(jsonContent).isEmpty();
    }

    @Test
    public void should_return_empty_when_parse_json_returns_null_correctly_async() throws Exception {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts/1").build();
        final Optional<JsonNode> jsonContent = httpClient.executeAndParseJsonAsync(request, n -> (JsonNode) null).get();
        softly.assertThat(jsonContent).isEmpty();
    }

    @Test
    public void should_throw_exception_with_details_when_an_error_occurs() {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts/1").build();
        final Optional<JsonNode> jsonContent = httpClient.executeAndParseJson(request, n -> null);
        softly.assertThat(jsonContent).isEmpty();
    }

    @Test
    public void should_throw_exception_with_details_when_an_error_occurs_async() throws Exception {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts/1").build();
        final Optional<JsonNode> jsonContent = httpClient.executeAndParseJsonAsync(request, n -> (JsonNode) null).get();
        softly.assertThat(jsonContent).isEmpty();
    }

    @Test
    public void should_post_and_parse_correctly() {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts").post(create(parse("application/json"), "{\"title\":\"toto\",\"body\":\"hahahah\"}")).build();
        final Optional<JsonNode> jsonContent = httpClient.executeAndParseJson(request, Function.identity());
        softly.assertThat(jsonContent).isPresent()
                .get()
                .hasToString("{\"title\":\"toto\",\"body\":\"hahahah\",\"id\":101}");

    }

    @Test
    public void should_post_and_parse_correctly_async() throws Exception {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts").post(create(parse("application/json"), "{\"title\":\"toto\",\"body\":\"hahahah\"}")).build();
        final Optional<JsonNode> jsonContent = httpClient.executeAndParseJsonAsync(request, Function.identity()).get();
        softly.assertThat(jsonContent).isPresent()
                .get()
                .hasToString("{\"title\":\"toto\",\"body\":\"hahahah\",\"id\":101}");

    }

    @Test
    public void should_execute_and_parse_json_handle_errors_with_details_correctly() {
        final Request request = new Request.Builder().url("https://api.github.com/search/repositories").build();
        assertThatExceptionOfType(HttpException.class)
                .isThrownBy(() -> httpClient.executeAndParseJson(request, Function.identity()))
                .withMessage("HTTP Error 422: {\"message\":\"Validation Failed\",\"errors\":[{\"resource\":\"Search\",\"field\":\"q\",\"code\":\"missing\"}],\"documentation_url\":\"https://developer.github.com/v3/search\"}.");
    }

    @Test
    public void should_execute_and_parse_json_handle_errors_with_details_correctly_async() throws Exception {
        final Request request = new Request.Builder().url("https://api.github.com/search/repositories").build();

        assertThatExceptionOfType(ExecutionException.class)
                .isThrownBy(() -> httpClient.executeAndParseJsonAsync(request, Function.identity()).get())
                .withCauseInstanceOf(HttpException.class)
                .withMessageEndingWith("HTTP Error 422: {\"message\":\"Validation Failed\",\"errors\":[{\"resource\":\"Search\",\"field\":\"q\",\"code\":\"missing\"}],\"documentation_url\":\"https://developer.github.com/v3/search\"}.");
    }

}
