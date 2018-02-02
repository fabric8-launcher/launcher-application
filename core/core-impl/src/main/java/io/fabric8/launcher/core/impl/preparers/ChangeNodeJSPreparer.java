package io.fabric8.launcher.core.impl.preparers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.ProjectilePreparer;

import static io.fabric8.launcher.core.spi.Application.ApplicationType.LAUNCHER;
import static java.nio.file.Files.*;
import static java.nio.file.Files.newBufferedReader;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createReader;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Application(LAUNCHER)
public class ChangeNodeJSPreparer implements ProjectilePreparer {

    @Override
    public void prepare(Path path, ProjectileContext context) {
        Path packageJsonPath = path.resolve("package.json");
        if (!exists(packageJsonPath)) {
            // package.json doesn't exist. Skip it
            return;
        }
        JsonObject packageJson;
        try (JsonReader reader = createReader(newBufferedReader(packageJsonPath))) {
            packageJson = reader.readObject();
            JsonObjectBuilder job = createObjectBuilder();
            job.add("name", context.getArtifactId());
            job.add("version", context.getProjectVersion());
            for (Map.Entry<String, JsonValue> entry : packageJson.entrySet()) {
                String key = entry.getKey();
                // Do not copy name or version
                if (key.equals("name") || key.equals("version")) {
                    continue;
                }
                job.add(key, entry.getValue());
            }
            writeContents(packageJsonPath, job);
        } catch (IOException e) {
            throw new RuntimeException("Error while changing Node.js metadata", e);
        }
    }


    private void writeContents(Path path, JsonObjectBuilder builder) throws IOException {
        JsonWriterFactory writerFactory = Json
                .createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));
        try (OutputStream out = newOutputStream(path);
             JsonWriter writer = writerFactory.createWriter(out)) {
            writer.writeObject(builder.build());
        }
    }
}
