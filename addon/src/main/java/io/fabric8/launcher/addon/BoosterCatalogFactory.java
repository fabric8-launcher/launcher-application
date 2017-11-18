/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.furnace.container.cdi.events.Local;
import org.jboss.forge.furnace.event.PostStartup;

import io.openshift.booster.catalog.BoosterCatalog;
import io.openshift.booster.catalog.BoosterCatalogService;

/**
 * Factory class for {@link BoosterCatalogService} objects
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class BoosterCatalogFactory
{
   public static final String CATALOG_GIT_REPOSITORY_PROPERTY_NAME = "LAUNCHPAD_BACKEND_CATALOG_GIT_REPOSITORY";
   public static final String CATALOG_GIT_REF_PROPERTY_NAME = "LAUNCHPAD_BACKEND_CATALOG_GIT_REF";
   public static final String LABEL_FILTERS_PROPERTY_NAME = "LAUNCHPAD_BACKEND_LABEL_FILTERS";

   private static final String DEFAULT_GIT_REPOSITORY_URL = "https://github.com/openshiftio/booster-catalog.git";
   private static final String DEFAULT_GIT_REF = "next";

   private BoosterCatalog defaultBoosterCatalog;

   private Map<CatalogServiceKey, BoosterCatalogService> cache = new ConcurrentHashMap<>();

   @Resource
   private ManagedExecutorService async;

   void init(@Observes @Local PostStartup startup)
   {
      // This will automatically call the reset method when constructed
   }

   @PostConstruct
   public void reset()
   {
      cache.clear();
      defaultBoosterCatalog = getCatalog(
               getEnvVarOrSysProp(CATALOG_GIT_REPOSITORY_PROPERTY_NAME, DEFAULT_GIT_REPOSITORY_URL),
               getEnvVarOrSysProp(CATALOG_GIT_REF_PROPERTY_NAME, DEFAULT_GIT_REF));
      // Index the openshift-online-free catalog
      if (!Boolean.getBoolean("LAUNCHPAD_SKIP_OOF_CATALOG_INDEX"))
      {
         getCatalog(getEnvVarOrSysProp(CATALOG_GIT_REPOSITORY_PROPERTY_NAME, DEFAULT_GIT_REPOSITORY_URL),
                  "openshift-online-free");
      }
   }

   @SuppressWarnings("unchecked")
   public String[] getFilterLabels(UIContext context)
   {
      Map<Object, Object> attributeMap = context.getAttributeMap();
      List<String> labels = (List<String>) attributeMap.get(LABEL_FILTERS_PROPERTY_NAME);
      if (labels != null && labels.size() > 0)
      {
         String filters = labels.get(0);
         if (filters.equals("all"))
         {
            // all is a special case which means that we don't want to apply
            // any filters.
            return new String[0];
         }
         return filters.split(",");
      }
      else
      {
         return new String[0];
      }
   }

   public BoosterCatalog getCatalog(UIContext context)
   {
      Map<Object, Object> attributeMap = context.getAttributeMap();
      String catalogUrl = (String) attributeMap.get(CATALOG_GIT_REPOSITORY_PROPERTY_NAME);
      String catalogRef = (String) attributeMap.get(CATALOG_GIT_REF_PROPERTY_NAME);
      if (catalogUrl == null && catalogRef == null)
      {
         return getDefaultCatalog();
      }
      return getCatalog(catalogUrl, catalogRef);
   }

   /**
    * @param catalogUrl the URL to use. Assumes {@link #DEFAULT_GIT_REPOSITORY_URL} if <code>null</code>
    * @param catalogRef the Git ref to use. Assumes {@link #DEFAULT_GIT_REF} if <code>null</code>
    * @return the {@link BoosterCatalogService} using the given catalog URL/ref tuple
    */
   public BoosterCatalog getCatalog(String catalogUrl, String catalogRef)
   {
      return cache.computeIfAbsent(
               new CatalogServiceKey(Objects.toString(catalogUrl, DEFAULT_GIT_REPOSITORY_URL),
                        Objects.toString(catalogRef, DEFAULT_GIT_REF)),
               key -> {
                  BoosterCatalogService service = new BoosterCatalogService.Builder()
                           .catalogRepository(key.getCatalogUrl())
                           .catalogRef(key.getCatalogRef())
                           .executor(async)
                           .build();
                  service.index();
                  return service;
               });
   }

   @Produces
   @Singleton
   public BoosterCatalog getDefaultCatalog()
   {
      return defaultBoosterCatalog;
   }

   private static String getEnvVarOrSysProp(String name, String defaultValue)
   {
      return System.getProperty(name, System.getenv().getOrDefault(name, defaultValue));
   }

   private class CatalogServiceKey
   {
      private final String catalogUrl;
      private final String catalogRef;

      /**
       * @param catalogUrl
       * @param catalogRef
       */
      public CatalogServiceKey(String catalogUrl, String catalogRef)
      {
         super();
         this.catalogUrl = catalogUrl;
         this.catalogRef = catalogRef;
      }

      /**
       * @return the catalogRef
       */
      public String getCatalogRef()
      {
         return catalogRef;
      }

      /**
       * @return the catalogUrl
       */
      public String getCatalogUrl()
      {
         return catalogUrl;
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((catalogRef == null) ? 0 : catalogRef.hashCode());
         result = prime * result + ((catalogUrl == null) ? 0 : catalogUrl.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         CatalogServiceKey other = (CatalogServiceKey) obj;
         if (catalogRef == null)
         {
            if (other.catalogRef != null)
               return false;
         }
         else if (!catalogRef.equals(other.catalogRef))
            return false;
         if (catalogUrl == null)
         {
            if (other.catalogUrl != null)
               return false;
         }
         else if (!catalogUrl.equals(other.catalogUrl))
            return false;
         return true;
      }
   }
}