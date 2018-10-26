package io.fabric8.launcher.core.spi;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ApplicationTypeConversionTest {

    @Test
    public void should_convert_to_launcher_when_header_not_specified() {
        // given
        String headerValue = null;

        // when
        final Application.ApplicationType recognizedApp = Application.ApplicationType.fromHeaderValue(headerValue);

        // then
        assertThat(recognizedApp).isEqualTo(Application.ApplicationType.LAUNCHER);
    }

    @Test
    public void should_convert_to_launcher_when_header_specified() {
        // given
        String headerValue = "Launcher";

        // when
        final Application.ApplicationType recognizedApp = Application.ApplicationType.fromHeaderValue(headerValue);

        // then
        assertThat(recognizedApp).isEqualTo(Application.ApplicationType.LAUNCHER);
    }

    @Test
    public void should_convert_to_osio_when_header_specified_ignoring_case() {
        // given
        String headerValue = "OSio";

        // when
        final Application.ApplicationType recognizedApp = Application.ApplicationType.fromHeaderValue(headerValue);

        // then
        assertThat(recognizedApp).isEqualTo(Application.ApplicationType.OSIO);
    }

    @Test
    public void should_throw_exception_when_header_set_to_unrecognized_app() {
        // given
        String headerValue = "mockito";

        // when
        final Throwable throwable = catchThrowable(() -> Application.ApplicationType.fromHeaderValue(headerValue));

        // then
        assertThat(throwable).hasMessageContaining("Unrecognized application. Header 'X-App' has an invalid value: MOCKITO");
    }
}
