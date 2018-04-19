package io.fabric8.launcher.base.http;

import io.fabric8.launcher.base.identity.Identity;
import okhttp3.Request;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public final class Requests {

    private Requests() {
        throw new IllegalAccessError("Utility class");
    }

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
}
