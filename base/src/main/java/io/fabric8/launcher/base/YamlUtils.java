package io.fabric8.launcher.base;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public final class YamlUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private YamlUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static <T> List<T> readList(Reader reader, Class<T> type) throws IOException {
        CollectionType collectionType = MAPPER.getTypeFactory().constructCollectionType(List.class, type);
        return MAPPER.readValue(reader, collectionType);
    }
}