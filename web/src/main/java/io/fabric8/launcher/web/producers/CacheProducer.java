package io.fabric8.launcher.web.producers;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.fabric8.launcher.core.spi.DirectoryReaper;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.event.CacheEntryExpiredListener;

@ApplicationScoped
public class CacheProducer {

    @Inject
    DirectoryReaper directoryReaper;

    @ApplicationScoped
    @Produces
    public Cache<String, AppPath> producePathCache() {
        return Cache2kBuilder.of(String.class, AppPath.class)
                .expireAfterWrite(1, TimeUnit.MINUTES)    // expire after 1 minute
                .resilienceDuration(30, TimeUnit.SECONDS) // cope with at most 30 seconds
                .addListener((CacheEntryExpiredListener<String, AppPath>) (cache, cacheEntry)
                        -> directoryReaper.delete(cacheEntry.getValue().path)) // Delete when entry expires
                .build();
    }

    public static class AppPath {
        public final String name;
        public final Path path;

        public AppPath(String name, Path path) {
            this.name = name;
            this.path = path;
        }
    }
}
