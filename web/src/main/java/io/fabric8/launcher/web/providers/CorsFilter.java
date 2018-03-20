package io.fabric8.launcher.web.providers;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

/**
 * CORS Filter
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@WebFilter(filterName = "CorsFilter", urlPatterns = "/*", asyncSupported = true)
public class CorsFilter implements Filter {

    private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, HEAD";

    private static final int MAX_AGE = 42 * 60 * 60;

    private static final String DEFAULT_ALLOWED_HEADERS = "*";

    private static final String DEFAULT_EXPOSED_HEADERS = "*";

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.addHeader("Access-Control-Allow-Origin", DEFAULT_ALLOWED_HEADERS);
        httpServletResponse.addHeader("Access-Control-Allow-Headers", DEFAULT_ALLOWED_HEADERS);
        httpServletResponse.addHeader("Access-Control-Expose-Headers", DEFAULT_EXPOSED_HEADERS);
        httpServletResponse.addHeader("Access-Control-Allow-Credentials", "true");
        httpServletResponse.addHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
        httpServletResponse.addIntHeader("Access-Control-Max-Age", MAX_AGE);
        chain.doFilter(request, response);
    }


    @Override
    public void destroy() {

    }
}
