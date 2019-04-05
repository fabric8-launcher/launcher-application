package io.fabric8.launcher.web.endpoints;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

@WebServlet(urlPatterns = "/openapi")
public class OpenAPIServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/yaml");
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/openapi.yml")) {
            IOUtils.copy(is, resp.getOutputStream());
        } finally {
            resp.flushBuffer();
        }
    }
}
