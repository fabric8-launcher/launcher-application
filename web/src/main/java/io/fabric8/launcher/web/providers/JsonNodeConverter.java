package io.fabric8.launcher.web.providers;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.launcher.base.JsonUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ext.ParamConverter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class JsonNodeConverter implements ParamConverter<JsonNode> {

    private static final Logger logger = Logger.getLogger(JsonNodeConverter.class.getName());

    @Override
    public JsonNode fromString(String value) {
        try {
            return JsonUtils.readTree(value);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error while reading JSON data", e);
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString(JsonNode value) {
        try {
            return JsonUtils.toString(value);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error while writing JSON data", e);
            throw new UncheckedIOException(e);
        }
    }
}
