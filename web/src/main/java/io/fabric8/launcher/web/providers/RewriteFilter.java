package io.fabric8.launcher.web.providers;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

@WebFilter(urlPatterns = "/*", initParams = {
        @WebInitParam(name = "confPath", value = "META-INF/urlrewrite.xml")
})
public class RewriteFilter extends UrlRewriteFilter {
}
