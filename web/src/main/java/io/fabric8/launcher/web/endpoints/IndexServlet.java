package io.fabric8.launcher.web.endpoints;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;

/**
 * Used to serve a modified index.html
 */
@WebServlet(urlPatterns = "/index.html", asyncSupported = true)
public class IndexServlet extends HttpServlet {

    String index;

    @Override
    public void init(ServletConfig config) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String settings;
        try (InputStream stream = classLoader.getResourceAsStream("META-INF/settings-tmpl.json")) {
            String settingsTemplate = IOUtils.toString(stream, Charset.defaultCharset());
            StringSubstitutor substitutor = new StringSubstitutor();
            substitutor.setVariableResolver(key -> System.getenv().getOrDefault(key, ""));
            settings = StringEscapeUtils.escapeJson(substitutor.replace(settingsTemplate)
                                                                 .replace("\n",""));
        } catch (IOException e) {
            e.printStackTrace();
            settings = "{}";
        }
        try (InputStream stream = config.getServletContext().getResourceAsStream("index.html")) {
            String indexHtml = IOUtils.toString(stream, Charset.defaultCharset());
            index = indexHtml.replace("$GLOBAL_CONFIG_JSON_STRING", settings);
        } catch (IOException e) {
            e.printStackTrace();
            index = "<h1>Error while rendering index.html. Check logs</h1>";
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        try (PrintWriter writer = resp.getWriter()) {
            writer.print(index);
            writer.flush();
        }
    }
}