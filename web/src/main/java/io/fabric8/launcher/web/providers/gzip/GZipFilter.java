package io.fabric8.launcher.web.providers.gzip;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@WebFilter(filterName = "GZipFilter", urlPatterns = "/*", asyncSupported = true)
public class GZipFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // If not HttpServletRequest, ignore
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (acceptsGZipEncoding(httpRequest)) {
            httpResponse.setHeader("Content-Encoding", "gzip");
            try (GZipServletResponseWrapper gzipResponse =
                         new GZipServletResponseWrapper(httpResponse)) {
                chain.doFilter(request, gzipResponse);
            }
        } else {
            chain.doFilter(request, response);
        }

    }

    private boolean acceptsGZipEncoding(HttpServletRequest httpRequest) {
        String acceptEncoding =
                httpRequest.getHeader("Accept-Encoding");

        return acceptEncoding != null &&
                acceptEncoding.contains("gzip");
    }


    @Override
    public void destroy() {

    }
}
