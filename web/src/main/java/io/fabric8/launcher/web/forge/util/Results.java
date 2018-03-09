package io.fabric8.launcher.web.forge.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.forge.addon.ui.result.CompositeResult;
import org.jboss.forge.addon.ui.result.Result;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public final class Results {

    private Results() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * @param result
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getEntityAsMap(Result result) {
        if (result instanceof CompositeResult) {
            for (Result singleResult : ((CompositeResult) result).getResults()) {
                Object obj = singleResult.getEntity().orElse(null);
                if (obj instanceof Map) {
                    return (Map<String, String>) obj;
                }
            }
        }
        return Collections.emptyMap();
    }


    public static Object getEntity(Result result) {
        if (result == null) {
            return null;
        } else if (result instanceof CompositeResult) {
            CompositeResult compositeResult = (CompositeResult) result;
            List<Object> answer = new ArrayList<>();
            List<Result> results = compositeResult.getResults();
            for (Result child : results) {
                Object entity = getEntity(child);
                answer.add(entity);
            }
            return answer;
        }
        return result.getEntity().orElse(null);
    }

    /**
     * Returns the result message handling composite results
     */
    public static String getMessage(Result result) {
        if (result == null) {
            return null;
        } else if (result instanceof CompositeResult) {
            CompositeResult compositeResult = (CompositeResult) result;
            StringBuilder builder = new StringBuilder();
            List<Result> results = compositeResult.getResults();
            for (Result child : results) {
                String message = getMessage(child);
                if (message != null && message.trim().length() > 0) {
                    if (builder.length() > 0) {
                        builder.append("\n");
                    }
                    builder.append(message);
                }
            }
            return builder.toString();

        }
        return result.getMessage();
    }

}
