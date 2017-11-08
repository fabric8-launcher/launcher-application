package io.openshift.appdev.missioncontrol.test;

import java.io.IOException;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import org.junit.Assert;

/**
 * Support used in testing
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
class TestSupport {

    private TestSupport() {
        // No instances
    }

    private static final Logger log = Logger.getLogger(TestSupport.class.getName());

    /**
     * Ensures that an expected HTTP client error status is generated when we
     * request the specified URL, else fail with the specified message
     *
     * @param url
     * @param expectedErrorStatus
     * @param failMessage
     * @throws IOException
     */
    static void assertHttpClientErrorStatus(final String url,
                                            final int expectedErrorStatus,
                                            final String failMessage)
            throws IOException {
        assert url != null && !url.isEmpty() : "url is required";
        assert expectedErrorStatus >= 400 && expectedErrorStatus < 500 :
                "expected error status must be in the 400 range";
        assert failMessage != null && !failMessage.isEmpty() : "failMessage must be specified";
        final WebClient webClient = new WebClient();
        try {
            webClient.getPage(url);
        } catch (final FailingHttpStatusCodeException fhsce) {
            final int httpStatus = fhsce.getStatusCode();
            log.info("Got HTTP status: " + httpStatus);
            Assert.assertEquals(expectedErrorStatus, httpStatus);
            return;
        }
        Assert.fail(failMessage);
    }

}
