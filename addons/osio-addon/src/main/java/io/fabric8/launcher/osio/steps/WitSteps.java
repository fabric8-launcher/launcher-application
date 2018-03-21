package io.fabric8.launcher.osio.steps;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.launcher.base.http.ExternalRequest;
import io.fabric8.launcher.base.http.HttpException;
import io.fabric8.launcher.osio.EnvironmentVariables;
import io.fabric8.launcher.osio.tenant.Tenant;
import io.fabric8.launcher.service.git.api.GitRepository;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RequestScoped
public class WitSteps {

    @Inject
    private Tenant tenant;

    private static final Logger logger = Logger.getLogger(WitSteps.class.getName());

    public void createCodebase(String spaceId, String stackId, GitRepository repository) {
        String payload = "{\n" +
                "  \"data\": {\n" +
                "    \"attributes\": {\n" +
                "      \"stackId\": \"" + stackId + "\",\n" +
                "      \"type\": \"git\",\n" +
                "      \"url\": \"" + repository.getGitCloneUri() + "\"\n" +
                "    },\n" +
                "    \"type\": \"codebases\"\n" +
                "  }\n" +
                "}";

        Request request = new Request.Builder()
                .url(EnvironmentVariables.ExternalServices.getCodebaseCreateURL(spaceId))
                .header("Authorization", "Bearer " + tenant.getIdentity().getToken())
                .post(RequestBody.create(MediaType.parse("application/json"), payload))
                .build();
        ExternalRequest.execute(request, response -> {
            if (!response.isSuccessful()) {
                assert response.body() != null;
                String message = response.message();
                try {
                    String body = response.body().string();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode errors = mapper.readTree(body).get("errors");
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw, true);
                    for (JsonNode error : errors) {
                        pw.println(error.get("detail").asText());
                    }
                    message = sw.toString();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error while reading error from WIT", e);
                }
                throw new HttpException(response.code(), message);
            }
            return null;
        });

    }
}
