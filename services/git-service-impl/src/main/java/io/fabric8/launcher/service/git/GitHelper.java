package io.fabric8.launcher.service.git;

import static io.fabric8.launcher.base.identity.IdentityHelper.createRequestAuthorizationHeaderKey;
import static io.fabric8.launcher.base.identity.IdentityHelper.createRequestAuthorizationHeaderValue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.launcher.base.identity.Identity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Helper class for Git
 */
public final class GitHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Predicate<String> GIT_FULLNAME_PREDICATE = Pattern.compile("^[a-zA-Z0-9-_]+/[a-zA-Z0-9-_]+$").asPredicate();

    private GitHelper(){
        throw new IllegalAccessError();
    }

    /**
     * Encode a param in order to add it to an URL
     *
     * @param param the param to encode
     * @return the encoded param
     */
    public static String encode(final String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Tell whether the given name is a valid git repository full name or not (<owner>/<repo>)
     *
     * @param name the git repository full name to check
     * @return true if it is a valid git repository full name
     */
    public static boolean isValidGitRepositoryFullName(final String name){
        return GIT_FULLNAME_PREDICATE.test(name);
    }

    /**
     * Check whether the given name is a valid git repository full name or not (<owner>/<repo>)
     *
     * @param fullName the git repository full name to check
     * @return the given name if it is valid
     * @throws IllegalArgumentException if this is not a valid git repository full name
     */
    public static String checkGitRepositoryFullNameArgument(final String fullName){
        if (!isValidGitRepositoryFullName(fullName)) {
            throw new IllegalArgumentException(String.format("This repository name is not a valid git repository full name: %s.", fullName));
        }
        return fullName;
    }

    public static String createGitRepositoryFullName(final String owner, final String name){
        return String.format("%s/%s", owner, name);
    }

    /**
     * Request builder factory prepared with identity authorization
     *
     * @param identity the {@link Identity to use}
     * @return the {@link Request.Builder}
     */
    public static Request.Builder request(final Identity identity) {
        final String authorizationKey = createRequestAuthorizationHeaderKey(identity);
        final String authorizationValue = createRequestAuthorizationHeaderValue(identity);
        return new Request.Builder().header(authorizationKey, authorizationValue);
    }

    /**
     * Execute the given request and parse the response {@link JsonNode} with the given consumer
     *
     * @param request the {@link Request} to execute
     * @param consumer the consumer to parse the {@link JsonNode}
     * @param <T> the type of the parsed result
     * @return the parsed result
     */
    public static <T> Optional<T> execute(Request request, Function<JsonNode, T> consumer) {
        OkHttpClient httpClient = new OkHttpClient();
        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (response.isSuccessful()) {
                if (body == null || consumer == null) {
                    return Optional.empty();
                }
                JsonNode tree = MAPPER.readTree(body.string());
                return Optional.ofNullable(consumer.apply(tree));
            } else if(response.code() == 404) {
                return Optional.empty();
            } else {
                final String details = body != null ? response.body().string() : "No details";
                throw new IllegalStateException(String.format("%s: %s.", response.message(), details));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


}
