package io.fabric8.launcher.service.git;

import java.nio.file.Paths;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class UnmappableCharsTest {

    @Test
    public void path_should_not_throw_invalid_path_exception() {
        assertThat(Paths.get("._Не подтвержден.tmp").toFile()).isNotNull();
    }
}
