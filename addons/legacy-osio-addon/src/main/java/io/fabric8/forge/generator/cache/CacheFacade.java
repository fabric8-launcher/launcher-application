/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.forge.generator.cache;

import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.eviction.EvictionType;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@Singleton
public class CacheFacade {
    private static final transient Logger LOG = LoggerFactory.getLogger(CacheFacade.class);

    private EmbeddedCacheManager manager = new DefaultCacheManager();

    @Inject
    @Singleton
    public CacheFacade() {
        manager.defineConfiguration(CacheNames.USER_NAMESPACES, createCacheConfiguration(1000, 2 * 60));
        manager.defineConfiguration(CacheNames.USER_SPACES, createCacheConfiguration(1000, 2 * 60));
        manager.defineConfiguration(CacheNames.USER_PROFILE_SETTINGS, createCacheConfiguration(1000, 60 * 5));

        manager.defineConfiguration(CacheNames.GIT_PROVIDERS, createCacheConfiguration(1000, 2 * 60));

        manager.defineConfiguration(CacheNames.GITHUB_ACCOUNT_FROM_SECRET, createCacheConfiguration(1000, 2 * 60));
        manager.defineConfiguration(CacheNames.GITHUB_ORGANISATIONS, createCacheConfiguration(1000, 60 * 5));
        manager.defineConfiguration(CacheNames.GITHUB_REPOSITORIES_FOR_ORGANISATION, createCacheConfiguration(1000, 60 * 5));

        manager.defineConfiguration(CacheNames.GOGS_ACCOUNT_FROM_SECRET, createCacheConfiguration(1000, 2 * 60));
        manager.defineConfiguration(CacheNames.GOGS_ORGANISATIONS, createCacheConfiguration(1000, 60 * 5));

        LOG.info("starting caches");
        manager.start();
    }


    @PreDestroy
    public void destroy() {
        LOG.info("stopping caches");
        manager.stop();
    }


    /**
     * Returns the cache for the given name, lazily creating it with a warning if its not been explicitly configured
     */
    public <K, V> Cache<K, V> getCache(String name) {
        return manager.getCache(name, true);
    }

    protected Configuration createCacheConfiguration(int cacheCount, int lifespanSeconds) {
        return new ConfigurationBuilder()
                .eviction().type(EvictionType.COUNT).size(cacheCount).
                        eviction().expiration().lifespan(lifespanSeconds, TimeUnit.SECONDS)
                .build();
    }

}
