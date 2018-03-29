package io.fabric8.launcher.service.git;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.launcher.base.http.ExternalRequest;
import io.fabric8.launcher.base.identity.Identity;
import okhttp3.Request;
import okhttp3.ResponseBody;

import static io.fabric8.launcher.base.identity.IdentityHelper.createRequestAuthorizationHeaderKey;
import static io.fabric8.launcher.base.identity.IdentityHelper.createRequestAuthorizationHeaderValue;
import static io.fabric8.launcher.service.git.api.GitService.GIT_FULLNAME_REGEXP;
import static io.fabric8.launcher.service.git.api.GitService.GIT_NAME_REGEXP;
import static java.util.Objects.requireNonNull;

/**
 * Helper class for Git
 */
public final class GitHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();



    private static final Predicate<String> GIT_FULLNAME_PREDICATE = Pattern.compile(GIT_FULLNAME_REGEXP).asPredicate();



    private static final Predicate<String> GIT_NAME_PREDICATE = Pattern.compile(GIT_NAME_REGEXP).asPredicate();

    private GitHelper() {
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
     * Tell whether the given name is a valid git repository name or not (<owner>/<repo>)
     *
     * @param name the git repository name to check
     * @return true if it is a valid git repository name
     */
    public static boolean isValidGitRepositoryName(final String name) {
        return GIT_NAME_PREDICATE.test(name);
    }

    /**
     * Check whether the given name is a valid git repository name or not.
     *
     * @param name the git repository name to check
     * @return the given name if it is valid
     * @throws IllegalArgumentException if this is not a valid git repository name
     * @throws NullPointerException     if this name is null
     */
    public static String checkGitRepositoryNameArgument(final String name) {
        requireNonNull(name, "name must not be null.");
        if (!isValidGitRepositoryName(name)) {
            throw new IllegalArgumentException(String.format("The given name is not a valid git repository name: %s.", name));
        }
        return name;
    }

    /**
     * Tell whether the given name is a valid git repository full name or not (<owner>/<repo>)
     *
     * @param fullName the git repository full name to check
     * @return true if it is a valid git repository full name
     */
    public static boolean isValidGitRepositoryFullName(final String fullName) {
        return GIT_FULLNAME_PREDICATE.test(fullName);
    }

    /**
     * Check whether the given name is a valid git repository full name or not (<owner>/<repo>).
     *
     * @param fullName the git repository full name to check
     * @return the given name if it is valid
     * @throws IllegalArgumentException if this is not a valid git repository full name
     * @throws NullPointerException     if this fullName is null
     */
    public static String checkGitRepositoryFullNameArgument(final String fullName) {
        requireNonNull(fullName, "fullName must not be null.");
        if (!isValidGitRepositoryFullName(fullName)) {
            throw new IllegalArgumentException(String.format("The given name is not a valid git repository full name: %s.", fullName));
        }
        return fullName;
    }

    public static String createGitRepositoryFullName(final String owner, final String name) {
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
     * @param request  the {@link Request} to execute
     * @param consumer the consumer to parse the {@link JsonNode}
     * @param <T>      the type of the parsed result
     * @return the parsed result
     */
    public static <T> Optional<T> execute(Request request, Function<JsonNode, T> consumer) {
        return ExternalRequest.executeAndMap(request, response -> {
            try {
                ResponseBody body = response.body();
                if (response.isSuccessful()) {
                    if (body == null || consumer == null) {
                        return Optional.empty();
                    }
                    JsonNode tree = MAPPER.readTree(body.string());
                    return Optional.ofNullable(consumer.apply(tree));
                } else if (response.code() == 404) {
                    return Optional.empty();
                } else {
                    final String details = body != null ? body.string() : "No details";
                    throw new IllegalStateException(String.format("%s: %s.", response.message(), details));
                }
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

        });
    }
}
