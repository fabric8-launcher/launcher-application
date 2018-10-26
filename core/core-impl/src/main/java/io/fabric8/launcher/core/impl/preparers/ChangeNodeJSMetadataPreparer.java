package io.fabric8.launcher.core.impl.preparers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.core.api.ProjectileContext;
import io.fabric8.launcher.core.api.projectiles.context.CoordinateCapable;
import io.fabric8.launcher.core.api.projectiles.context.ProjectNameCapable;
import io.fabric8.launcher.core.spi.ProjectilePreparer;

/**
 * Called by {@link io.fabric8.launcher.core.api.MissionControl#prepare(ProjectileContext)}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class ChangeNodeJSMetadataPreparer implements ProjectilePreparer {

    @Override
    public void prepare(Path projectPath, RhoarBooster booster, ProjectileContext context) {
        if (!(context instanceof CoordinateCapable) && !(context instanceof ProjectNameCapable)) {
            return;
        }

        CoordinateCapable coordinateCapable = (CoordinateCapable) context;
        Path packageJsonPath = projectPath.resolve("package.json");
        if (Files.exists(packageJsonPath)) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("name", ((ProjectNameCapable)context).getProjectName());
            job.add("version", coordinateCapable.getProjectVersion());
            try (BufferedReader bufferedReader = Files.newBufferedReader(packageJsonPath);
                 JsonReader reader = Json.createReader(bufferedReader)) {
                for (Map.Entry<String, JsonValue> entry : reader.readObject().entrySet()) {
                    String key = entry.getKey();
                    // Do not copy name or version
                    if (key.equals("name") || key.equals("version")) {
                        continue;
                    }
                    job.add(key, entry.getValue());
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Error while reading " + packageJsonPath, e);
            }

            JsonWriterFactory writerFactory = Json.createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));
            try (OutputStream out = Files.newOutputStream(packageJsonPath);
                 JsonWriter writer = writerFactory.createWriter(out)) {
                writer.write(job.build());
            } catch (IOException e) {
                throw new UncheckedIOException("Error while writing " + packageJsonPath, e);
            }
        }
    }
}
