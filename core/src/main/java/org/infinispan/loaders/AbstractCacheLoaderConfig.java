package org.infinispan.loaders;

import org.infinispan.CacheException;
import org.infinispan.config.ConfigurationAttribute;
import org.infinispan.config.PluggableConfigurationComponent;

/**
 * Abstract base class for CacheLoaderConfigs.
 *
 * @author Mircea.Markus@jboss.com
 * @since 4.0
 */
public class AbstractCacheLoaderConfig extends PluggableConfigurationComponent implements CacheLoaderConfig {

   protected String cacheLoaderClassName;

   public String getCacheLoaderClassName() {
      return cacheLoaderClassName;
   }

   @ConfigurationAttribute(name = "class", 
            containingElement = "loader", 
            description = "Full class name of a cache loader")
   public void setCacheLoaderClassName(String className) {
      if (className == null || className.length() == 0) return;
      testImmutability("cacheLoaderClassName");
      this.cacheLoaderClassName = className;
   }

   @Override
   public AbstractCacheLoaderConfig clone() {
      try {
         return (AbstractCacheLoaderConfig) super.clone();
      } catch (CloneNotSupportedException e) {
         throw new CacheException(e);
      }
   }
}
