package io.openshift.appdev.missioncontrol.service.github.impl.kohsuke;

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

    private final OkUrlFactory urlFactory;

    public OkHttp3Connector(OkHttpClient client) {
        this.urlFactory = new OkUrlFactory(client);
    }

    @Override
    public HttpURLConnection connect(URL url) throws IOException {
        return urlFactory.open(url);
    }
}
