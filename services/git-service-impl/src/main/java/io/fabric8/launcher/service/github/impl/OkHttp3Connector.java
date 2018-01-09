package io.fabric8.launcher.service.github.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.OkUrlFactory;
import org.kohsuke.github.HttpConnector;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class OkHttp3Connector implements HttpConnector {

    public OkHttp3Connector(OkHttpClient client) {
        this.urlFactory = new OkUrlFactory(client);
    }

    private final OkUrlFactory urlFactory;

    @Override
    public HttpURLConnection connect(URL url) throws IOException {
        return urlFactory.open(url);
    }
}
