package io.fabric8.launcher.base.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import io.fabric8.launcher.base.identity.Identity;
import okhttp3.MediaType;
import okhttp3.Request;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public final class Requests {

    private Requests() {
        throw new IllegalAccessError("Utility class");
    }

    public static final MediaType APPLICATION_FORM_URLENCODED = MediaType.parse("application/x-www-form-urlencoded");

    public static final MediaType APPLICATION_JSON = MediaType.parse("application/json");

    /**
     * Request builder factory prepared with identity authorization
     *
     * @param identity          the {@link Identity} to use
     * @param authorizationType the {@link AuthorizationType} to use
     * @return the {@link Request.Builder}
     */
    public static Request.Builder securedRequest(final Identity identity, final AuthorizationType authorizationType) {
        return new Request.Builder().header(authorizationType.getHeaderName(), identity.toRequestAuthorization(authorizationType));
    }

    /**
     * Request builder factory prepared with default identity authorization {@link Identity#toRequestAuthorization()}
     *
     * @param identity the {@link Identity} to use
     * @return the {@link Request.Builder}
     */
    public static Request.Builder securedRequest(final Identity identity) {
        return new Request.Builder().header(identity.getDefaultAuthorizationType().getHeaderName(), identity.toRequestAuthorization());
    }

    /**
     * Encode a param in order to add it to an URL
     *
     * @param param the param to encode
     * @return the encoded param
     */
    public static String urlEncode(final String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
