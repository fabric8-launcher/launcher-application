package io.fabric8.launcher.core.impl.documentation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BoosterDocumentationStoreImplTest {

    @Test
    public void shouldReloadDocumentationCorrectly() throws Exception {
        //Given a downloader with two versions and a documentation store
        final Supplier<Path> mockDownloader = (Supplier<Path>) mock(Supplier.class);
        final Path expectedFirstPath = Paths.get("/v1");
        final Path expectedSecondPath = Paths.get("/v2");
        when(mockDownloader.get())
                .thenReturn(expectedFirstPath)
                .thenReturn(expectedSecondPath);
        final BoosterDocumentationStoreImpl boosterDocumentationStore = new BoosterDocumentationStoreImpl(ForkJoinPool.commonPool(), mockDownloader);

        //When getting the documentation path future
        final CompletableFuture<Path> firstFuture = boosterDocumentationStore.getDocumentationPath();

        //Then the returned path is the first one
        assertThat(firstFuture.get()).isEqualTo(expectedFirstPath);

        //When getting the documentation path again
        final CompletableFuture<Path> secondFuture = boosterDocumentationStore.getDocumentationPath();

        //Then the returned future has not changes and neither the path
        assertThat(secondFuture).isSameAs(firstFuture);
        assertThat(secondFuture.get()).isEqualTo(expectedFirstPath);
        verify(mockDownloader, times(1)).get();

        //When reloading the documentation
        boosterDocumentationStore.reloadDocumentation();

        //Then the returned future has changes and the path too
        final CompletableFuture<Path> futureAfterReload = boosterDocumentationStore.getDocumentationPath();
        assertThat(futureAfterReload).isNotSameAs(firstFuture);
        assertThat(futureAfterReload.get()).isEqualTo(expectedSecondPath);
        verify(mockDownloader, times(2)).get();
    }

    @Test
    public void shouldInitializeWithoutWaitingButWaitForDocumentationCorrectly() throws Exception {
        //Given a sleepy downloader with a counter and a documentation store
        final Supplier<Path> mockDownloader = (Supplier<Path>) mock(Supplier.class);
        final AtomicInteger counter = new AtomicInteger();
        when(mockDownloader.get()).thenAnswer(invocationOnMock -> {
            Thread.sleep(200);
            counter.incrementAndGet();
            return Paths.get("/v1");
        });

        final BoosterDocumentationStoreImpl boosterDocumentationStore = new BoosterDocumentationStoreImpl(ForkJoinPool.commonPool(), mockDownloader);

        //When initializing
        boosterDocumentationStore.initialize();

        //Then the counter has not been incremented yet
        assertThat(counter.get()).isEqualTo(0);

        //When waiting
        boosterDocumentationStore.waitForDocumentation();

        //Then the counter has been incremented
        assertThat(counter.get()).isEqualTo(1);
    }
}