package io.fabric8.launcher.web.providers;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@WebFilter(filterName = "EnvirontmentVarFilter", urlPatterns = "/index.html", asyncSupported = true)
public class EnvirontmentVarFilter implements Filter {


    private Configuration cfg;

    @Override
    public void init(FilterConfig filterConfig) {
        cfg = new Configuration(Configuration.VERSION_2_3_27);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        cfg.setServletContextForTemplateLoading(servletRequest.getServletContext(), "/");
        Template template = cfg.getTemplate("index.fmt");

        Writer out = new OutputStreamWriter(servletResponse.getOutputStream());
        Map<String, Object> model = new HashMap<>();
        model.put("GLOBAL_CONFIG_JSON_STRING", "{some: 'json'}");
        try {
            template.process(model, out);
        } catch (TemplateException e) {
            throw new ServletException("could not apply freemarker template", e);
        }
    }
}
