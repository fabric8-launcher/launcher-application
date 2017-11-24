package io.fabric8.launcher.web.forge.util;

import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.json.JsonWriter;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.furnace.versions.Versions;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class JsonOperations {

    public static JsonObject exceptionToJson(Throwable e, int depth) {
        JsonArrayBuilder stackElements = createArrayBuilder();
        StackTraceElement[] stackTrace = e.getStackTrace();
        JsonObjectBuilder builder = createObjectBuilder()
                .add("type", e.getClass().getName());

        add(builder, "message", e.getMessage());
        add(builder, "localizedMessage", e.getLocalizedMessage());
        add(builder, "forgeVersion", Versions.getImplementationVersionFor(UIContext.class).toString());

        if (stackTrace != null) {
            for (StackTraceElement element : stackTrace) {
                stackElements.add(stackTraceElementToJson(element));
            }
            builder.add("stackTrace", stackElements);
        }

        if (depth > 0) {
            Throwable cause = e.getCause();
            if (cause != null && cause != e) {
                builder.add("cause", exceptionToJson(cause, depth - 1));
            }
        }
        if (e instanceof WebApplicationException) {
            WebApplicationException webApplicationException = (WebApplicationException) e;
            Response response = webApplicationException.getResponse();
            if (response != null) {
                builder.add("status", response.getStatus());
            }
        }
        return builder.build();
    }

    public static Object unwrapJsonObjects(Object entity) {
        if (entity instanceof JsonObjectBuilder) {
            JsonObjectBuilder jsonObjectBuilder = (JsonObjectBuilder) entity;
            entity = jsonObjectBuilder.build();
        }
        if (entity instanceof JsonStructure) {
            StringWriter buffer = new StringWriter();
            JsonWriter writer = Json.createWriter(buffer);
            writer.write((JsonStructure) entity);
            writer.close();
            return buffer.toString();
        }
        return entity;
    }

    private static JsonObjectBuilder stackTraceElementToJson(StackTraceElement element) {
        JsonObjectBuilder builder = createObjectBuilder().add("line", element.getLineNumber());
        add(builder, "class", element.getClassName());
        add(builder, "file", element.getFileName());
        add(builder, "method", element.getMethodName());
        return builder;
    }

    private static void add(JsonObjectBuilder builder, String key, String value) {
        if (value != null) {
            builder.add(key, value);
        }
    }

}
