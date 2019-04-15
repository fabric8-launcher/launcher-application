package io.fabric8.launcher.base;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class JsonUtilsTest {

    @Test
    void testToArrayNode() {
        final ArrayNode node = JsonUtils.toArrayNode(Arrays.asList("A", "B", "C"));
        assertThat(node).isNotNull();
        assertThat(StreamSupport.stream(node.spliterator(), false).map(JsonNode::asText)).containsSequence("A", "B", "C");
    }

    @Test
    void testToObjectNode() {
        final ObjectNode node = JsonUtils.toObjectNode(Collections.singletonMap("key", "value"));
        assertThat(node).isNotNull();
        assertThat(node.get("key").asText()).isEqualTo("value");
    }

    @Test
    void testCreateArrayNode() {
        assertThat(JsonUtils.createArrayNode()).isNotNull();
    }

    @Test
    void testCreateObjectNode(){
        assertThat(JsonUtils.createObjectNode()).isNotNull();
    }

    @Test
    void testWriteTree(@TempDir Path tempDirectory) throws IOException {
        ObjectNode node = JsonUtils.createObjectNode().put("a",1);
        final Path path = tempDirectory.resolve("temp.json");
        JsonUtils.writeTree(node,path.toFile());
        assertThat(path).hasContent("{\n  \"a\" : 1\n}");
    }

}
