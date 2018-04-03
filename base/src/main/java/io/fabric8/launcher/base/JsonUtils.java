package io.fabric8.launcher.base;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

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

    /**
     * Converts a <code>Map</code> of type <code>&lt;String, Object&gt;</code>
     * to a <code>JsonObject</code>. The values of the object can itself be
     * objects, arrays or simple values
     */
    @SuppressWarnings("unchecked")
    public static JsonObjectBuilder toJsonObjectBuilder(Map<String, Object> map) {
        JsonObjectBuilder builder = createObjectBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                builder.add(entry.getKey(), toJsonObjectBuilder((Map<String, Object>) entry.getValue()));
            } else if (entry.getValue() instanceof Iterable) {
                builder.add(entry.getKey(), toJsonArrayBuilder((Iterable<Object>) entry.getValue()));
            } else if (entry.getValue() == null) {
                builder.addNull(entry.getKey());
            } else if (entry.getValue() instanceof Boolean) {
                builder.add(entry.getKey(), (Boolean) entry.getValue());
            } else if (entry.getValue() instanceof Double) {
                builder.add(entry.getKey(), (Double) entry.getValue());
            } else if (entry.getValue() instanceof Long) {
                builder.add(entry.getKey(), (Long) entry.getValue());
            } else if (entry.getValue() instanceof Integer) {
                builder.add(entry.getKey(), (Integer) entry.getValue());
            } else if (entry.getValue() instanceof BigInteger) {
                builder.add(entry.getKey(), (BigInteger) entry.getValue());
            } else if (entry.getValue() instanceof BigDecimal) {
                builder.add(entry.getKey(), (BigDecimal) entry.getValue());
            } else {
                builder.add(entry.getKey(), entry.getValue().toString());
            }
        }
        return builder;
    }

    /**
     * Converts any <code>Iterable</code> of type <code>&lt;Object&gt;</code>
     * to a <code>JsonArray</code>. The items of the array can itself be objects,
     * arrays or simple values
     */
    @SuppressWarnings("unchecked")
    public static JsonArrayBuilder toJsonArrayBuilder(Iterable<Object> list) {
        JsonArrayBuilder builder = createArrayBuilder();
        for (Object item : list) {
            if (item instanceof Map) {
                builder.add(toJsonObjectBuilder((Map<String, Object>) item));
            } else if (item instanceof Iterable) {
                builder.add(toJsonArrayBuilder((Iterable<Object>) item));
            } else if (item == null) {
                builder.addNull();
            } else if (item instanceof Boolean) {
                builder.add((Boolean) item);
            } else if (item instanceof Double) {
                builder.add((Double) item);
            } else if (item instanceof Long) {
                builder.add((Long) item);
            } else if (item instanceof Integer) {
                builder.add((Integer) item);
            } else if (item instanceof BigInteger) {
                builder.add((BigInteger) item);
            } else if (item instanceof BigDecimal) {
                builder.add((BigDecimal) item);
            } else {
                builder.add(item.toString());
            }
        }
        return builder;
    }
}
