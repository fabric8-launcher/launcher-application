package io.fabric8.launcher.base.http;

import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.launcher.base.test.hoverfly.LauncherPerTestHoverflyRule;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import okhttp3.Request;
import org.assertj.core.api.Assertions;
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

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class RequestsTest {

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


    @Test
    public void shouldExecuteCorrectly() {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com").build();
        assertThat(Requests.execute(request)).isTrue();
    }

    @Test
    public void shouldExecuteAndFailCorrectly() {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/not-found").build();
        assertThat(Requests.execute(request)).isFalse();
    }

    @Test
    public void shouldReadJsonCorrectly() {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts/1").build();
        final Optional<JsonNode> jsonContent = Requests.executeAndParseJson(request, Function.identity());
        softly.assertThat(jsonContent).isPresent()
                .get()
                .hasToString("{\"userId\":1,\"id\":1,\"title\":\"sunt aut facere repellat provident occaecati excepturi optio reprehenderit\",\"body\":\"quia et suscipit\\nsuscipit recusandae consequuntur expedita et cum\\nreprehenderit molestiae ut ut quas totam\\nnostrum rerum est autem sunt rem eveniet architecto\"}");
    }

    @Test
    public void shouldReturnEmptyWhenNotFoundUsingParseJsonCorrectly() {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts/not-found").build();
        final Optional<JsonNode> jsonContent = Requests.executeAndParseJson(request, Function.identity());
        softly.assertThat(jsonContent).isEmpty();
    }

    @Test
    public void shouldReturnEmptyWhenParseJsonReturnsNullCorrectly() {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts/1").build();
        final Optional<JsonNode> jsonContent = Requests.executeAndParseJson(request, n -> null);
        softly.assertThat(jsonContent).isEmpty();
    }

    @Test
    public void shouldThrowExceptionWithDetailsWhenAnErrorOccurs() {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts/1").build();
        final Optional<JsonNode> jsonContent = Requests.executeAndParseJson(request, n -> null);
        softly.assertThat(jsonContent).isEmpty();
    }

    @Test
    public void shouldPostAndParseCorrectly() {
        final Request request = new Request.Builder().url("https://jsonplaceholder.typicode.com/posts").post(create(parse("application/json"), "{\"title\":\"toto\",\"body\":\"hahahah\"}")).build();
        final Optional<JsonNode> jsonContent = Requests.executeAndParseJson(request, Function.identity());
        softly.assertThat(jsonContent).isPresent()
                .get()
                .hasToString("{\"title\":\"toto\",\"body\":\"hahahah\",\"id\":101}");

    }

    @Test
    public void shouldExecuteAndParseJsonHandleErrorsWithDetailsCorrectly() {
        final Request request = new Request.Builder().url("https://api.github.com/search/repositories").build();
        Assertions.assertThatExceptionOfType(HttpException.class)
                .isThrownBy(() -> Requests.executeAndParseJson(request, Function.identity()))
                .withMessage("HTTP Error 422: {\"message\":\"Validation Failed\",\"errors\":[{\"resource\":\"Search\",\"field\":\"q\",\"code\":\"missing\"}],\"documentation_url\":\"https://developer.github.com/v3/search\"}.");
    }

}
