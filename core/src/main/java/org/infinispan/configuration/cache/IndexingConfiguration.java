package org.infinispan.configuration.cache;

import static org.infinispan.configuration.parsing.Element.INDEXING;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.infinispan.commons.configuration.AbstractTypedPropertiesConfiguration;
import org.infinispan.commons.configuration.ConfigurationInfo;
import org.infinispan.commons.configuration.attributes.Attribute;
import org.infinispan.commons.configuration.attributes.AttributeDefinition;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.commons.configuration.attributes.CollectionAttributeCopier;
import org.infinispan.commons.configuration.attributes.Matchable;
import org.infinispan.commons.configuration.elements.DefaultElementDefinition;
import org.infinispan.commons.configuration.elements.ElementDefinition;
import org.infinispan.commons.util.TypedProperties;

/**
 * Configures indexing of entries in the cache for searching.
 */
public class IndexingConfiguration extends AbstractTypedPropertiesConfiguration implements Matchable<IndexingConfiguration>, ConfigurationInfo {
   public static final AttributeDefinition<Index> INDEX = AttributeDefinition.builder("index", Index.NONE).immutable().build();
   public static final AttributeDefinition<Boolean> AUTO_CONFIG = AttributeDefinition.builder("autoConfig", false).immutable().build();
   public static final AttributeDefinition<Map<Class<?>, Class<?>>> KEY_TRANSFORMERS = AttributeDefinition.builder("key-transformers", null, (Class<Map<Class<?>, Class<?>>>) (Class<?>) Map.class)
         .copier(CollectionAttributeCopier.INSTANCE)
         .initializer(HashMap::new).immutable().build();
   public static final AttributeDefinition<Set<Class<?>>> INDEXED_ENTITIES = AttributeDefinition.builder("indexed-entities", null, (Class<Set<Class<?>>>) (Class<?>) Set.class)
         .copier(CollectionAttributeCopier.INSTANCE)
         .initializer(HashSet::new).immutable().build();

   static AttributeSet attributeDefinitionSet() {
      return new AttributeSet(IndexingConfiguration.class, AbstractTypedPropertiesConfiguration.attributeSet(), INDEX, AUTO_CONFIG, KEY_TRANSFORMERS, INDEXED_ENTITIES);
   }

   static final ElementDefinition ELEMENT_DEFINITION = new DefaultElementDefinition(INDEXING.getLocalName());

   private static final String DIRECTORY_PROVIDER_KEY = "directory_provider";

   /**
    * Legacy name "ram" was replaced by "local-heap"
    */
   @Deprecated
   private static final String RAM_DIRECTORY_PROVIDER = "ram";
   private static final String LOCAL_HEAP_DIRECTORY_PROVIDER = "local-heap";
   private static final String LOCAL_HEAP_DIRECTORY_PROVIDER_FQN = "org.hibernate.search.store.impl.RAMDirectoryProvider";

   private final Attribute<Index> index;
   private final Attribute<Boolean> autoConfig;
   private final Attribute<Map<Class<?>, Class<?>>> keyTransformers;
   private final Attribute<Set<Class<?>>> indexedEntities;

   IndexingConfiguration(AttributeSet attributes) {
      super(attributes);
      index = attributes.attribute(INDEX);
      autoConfig = attributes.attribute(AUTO_CONFIG);
      keyTransformers = attributes.attribute(KEY_TRANSFORMERS);
      indexedEntities = attributes.attribute(INDEXED_ENTITIES);
   }

   /**
    * Whether indexing is enabled. False by default.
    *
    * @deprecated Use {@link #index()} instead
    */
   @Deprecated
   public boolean enabled() {
      return index().isEnabled();
   }

   /**
    * If true, only index changes made locally, ignoring remote changes. This is useful if indexes
    * are shared across a cluster to prevent redundant indexing of updates.
    *
    * @deprecated Use {@link #index()} instead
    */
   @Deprecated
   public boolean indexLocalOnly() {
      return index().isLocalOnly();
   }

   @Override
   public ElementDefinition getElementDefinition() {
      return ELEMENT_DEFINITION;
   }

   /**
    * <p>
    * These properties are passed directly to the embedded Hibernate Search engine, so for the
    * complete and up to date documentation about available properties refer to the Hibernate Search
    * reference of the version you're using with Infinispan Query.
    * </p>
    *
    * @see <a
    *      href="http://docs.jboss.org/hibernate/stable/search/reference/en-US/html_single/">Hibernate
    *      Search</a>
    */
   @Override
   public TypedProperties properties() {
      // Overridden to replace Javadoc
      return super.properties();
   }

   /**
    * Returns the indexing mode of this cache.
    */
   public Index index() {
      return index.get();
   }

   /**
    * Determines if autoconfig is enabled for this IndexingConfiguration.
    */
   public boolean autoConfig() {
      return autoConfig.get();
   }

   /**
    * The currently configured key transformers.
    *
    * @return a {@link Map} in which the map key is the key class and the value is the Transformer class.
    */
   public Map<Class<?>, Class<?>> keyTransformers() {
      return keyTransformers.get();
   }

   public Set<Class<?>> indexedEntities() {
      return indexedEntities.get();
   }

   public AttributeSet attributes() {
      return attributes;
   }

   /**
    * Check if the indexes can be shared. Currently only "ram" based indexes don't allow any sort of
    * sharing.
    *
    * @return false if the index is ram only and thus not shared
    */
   public boolean indexShareable() {
      TypedProperties properties = properties();
      boolean hasRamDirectoryProvider = false;
      for (Object objKey : properties.keySet()) {
         String key = (String) objKey;
         if (key.endsWith(DIRECTORY_PROVIDER_KEY)) {
            String directoryImplementationName = String.valueOf(properties.get(key)).trim();
            if (LOCAL_HEAP_DIRECTORY_PROVIDER.equalsIgnoreCase(directoryImplementationName)
                  || RAM_DIRECTORY_PROVIDER.equalsIgnoreCase(directoryImplementationName)
                  || LOCAL_HEAP_DIRECTORY_PROVIDER_FQN.equals(directoryImplementationName)) {
               hasRamDirectoryProvider = true;
            } else {
               return true;
            }
         }
      }
      return !hasRamDirectoryProvider;
   }

   @Override
   public String toString() {
      return "IndexingConfiguration [attributes=" + attributes + "]";
   }
}
