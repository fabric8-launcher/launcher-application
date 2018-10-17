package io.fabric8.launcher.core.spi;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationTypeConversionTest {

    @Test
    public void should_convert_to_launcher_when_header_not_specified() {
        // given
        final HttpServletRequest requestWithoutHeader = mock(HttpServletRequest.class);
        when(requestWithoutHeader.getHeader("X-App")).thenReturn(null);

        // when
        final Application.ApplicationType recognizedApp = Application.ApplicationType.fromHeader(requestWithoutHeader);

        // then
        assertThat(recognizedApp).isEqualTo(Application.ApplicationType.LAUNCHER);
    }

    @Test
    public void should_convert_to_launcher_when_header_specified() {
        // given
        final HttpServletRequest requestWithoutHeader = mock(HttpServletRequest.class);
        when(requestWithoutHeader.getHeader("X-App")).thenReturn("Launcher");

        // when
        final Application.ApplicationType recognizedApp = Application.ApplicationType.fromHeader(requestWithoutHeader);

        // then
        assertThat(recognizedApp).isEqualTo(Application.ApplicationType.LAUNCHER);
    }

    @Test
    public void should_convert_to_osio_when_header_specified_ignoring_case() {
        // given
        final HttpServletRequest requestWithoutHeader = mock(HttpServletRequest.class);
        when(requestWithoutHeader.getHeader("X-App")).thenReturn("OSio");

        // when
        final Application.ApplicationType recognizedApp = Application.ApplicationType.fromHeader(requestWithoutHeader);

        // then
        assertThat(recognizedApp).isEqualTo(Application.ApplicationType.OSIO);
    }

    @Test
    public void should_throw_exception_when_header_set_to_unrecognized_app() {
        // given
        final HttpServletRequest requestWithoutHeader = mock(HttpServletRequest.class);
        when(requestWithoutHeader.getHeader("X-App")).thenReturn("mockito");

        // when
        final Throwable throwable = catchThrowable(() -> Application.ApplicationType.fromHeader(requestWithoutHeader));

        // then
        assertThat(throwable).hasMessageContaining("Unrecognized application. Header 'X-App' has an invalid value: MOCKITO");
    }
}
