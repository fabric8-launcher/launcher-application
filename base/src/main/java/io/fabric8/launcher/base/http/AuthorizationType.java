package io.fabric8.launcher.base.http;

public enum AuthorizationType {
    BASIC("Authorization"),
    BEARER_TOKEN("Authorization"),
    PRIVATE_TOKEN("Private-Token");

    private final String headerName;

    AuthorizationType(final String headerName) {
        this.headerName = headerName;
    }

    public String getHeaderName() {
        return headerName;
    }
}
