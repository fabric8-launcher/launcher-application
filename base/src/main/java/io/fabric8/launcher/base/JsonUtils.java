package io.fabric8.launcher.base;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;

public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static String toString(Object obj) throws IOException {
        return MAPPER.writeValueAsString(obj);
    }

    public static JsonNode readTree(String content) throws IOException {
        return MAPPER.readTree(content);
    }

    public static void writeTree(JsonNode node, File target) throws IOException {
        final ObjectWriter writer = MAPPER.writer(new DefaultPrettyPrinter());
        writer.writeValue(target, node);
    }

    /**
     * Iterates on every item in the given {@link JsonNode} applying the given {@link Function}
     * and transforms into a {@link List}
     */
    public static <T> List<T> toList(JsonNode node, Function<JsonNode, T> transformer) {
        return stream(node.spliterator(), false).map(transformer).collect(Collectors.toList());
    }

    /**
     * Transforms the given {@link ObjectNode} into a {@link Map}
     */
    public static Map<String,Object> toMap(ObjectNode node) {
        return (Map<String,Object>)MAPPER.convertValue(node, Map.class);
    }


    /**
     * @return a new {@link ArrayNode}
     */
    public static ArrayNode createArrayNode() {
        return MAPPER.createArrayNode();
    }

    /**
     * @return a new {@link ObjectNode}
     */
    public static ObjectNode createObjectNode() {
        return MAPPER.createObjectNode();
    }

    /**
     * Converts a <code>Map</code> of type <code>&lt;String, Object&gt;</code>
     * to an <code>ObjectNode</code>. The values of the object can itself be
     * objects, arrays or simple values
     */
    @SuppressWarnings("unchecked")
    public static ObjectNode toObjectNode(Map<String, Object> map) {
        final ObjectNode objectNode = MAPPER.createObjectNode();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                objectNode.set(entry.getKey(), toObjectNode((Map<String, Object>) entry.getValue()));
            } else if (entry.getValue() instanceof Iterable) {
                objectNode.set(entry.getKey(), toArrayNode((Iterable<Object>) entry.getValue()));
            } else if (entry.getValue() == null) {
                objectNode.putNull(entry.getKey());
            } else if (entry.getValue() instanceof Boolean) {
                objectNode.put(entry.getKey(), (Boolean) entry.getValue());
            } else if (entry.getValue() instanceof Double) {
                objectNode.put(entry.getKey(), (Double) entry.getValue());
            } else if (entry.getValue() instanceof Long) {
                objectNode.put(entry.getKey(), (Long) entry.getValue());
            } else if (entry.getValue() instanceof Integer) {
                objectNode.put(entry.getKey(), (Integer) entry.getValue());
            } else if (entry.getValue() instanceof BigInteger) {
                objectNode.put(entry.getKey(), (BigInteger) entry.getValue());
            } else if (entry.getValue() instanceof BigDecimal) {
                objectNode.put(entry.getKey(), (BigDecimal) entry.getValue());
            } else {
                objectNode.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return objectNode;
    }

    /**
     * Converts any <code>Iterable</code> to an <code>ArrayNode</code>.
     * The items of the array can itself be objects, arrays or simple values
     */
    @SuppressWarnings("unchecked")
    public static ArrayNode toArrayNode(Iterable<?> list) {
        final ArrayNode arrayNode = MAPPER.createArrayNode();
        for (Object item : list) {
            if (item instanceof Map) {
                arrayNode.add(toObjectNode((Map<String, Object>) item));
            } else if (item instanceof Iterable) {
                arrayNode.add(toArrayNode(((Iterable<Object>) item)));
            } else if (item == null) {
                arrayNode.addNull();
            } else if (item instanceof Boolean) {
                arrayNode.add((Boolean) item);
            } else if (item instanceof Double) {
                arrayNode.add((Double) item);
            } else if (item instanceof Long) {
                arrayNode.add((Long) item);
            } else if (item instanceof Integer) {
                arrayNode.add((Integer) item);
            } else if (item instanceof BigInteger) {
                arrayNode.add((BigInteger) item);
            } else if (item instanceof BigDecimal) {
                arrayNode.add((BigDecimal) item);
            } else {
                arrayNode.add(item.toString());
            }
        }
        return arrayNode;
    }
}
