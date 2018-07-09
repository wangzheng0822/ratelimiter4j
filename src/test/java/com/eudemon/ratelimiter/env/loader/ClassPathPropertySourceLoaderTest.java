package com.eudemon.ratelimiter.env.loader;

import com.eudemon.ratelimiter.env.PropertySource;
import com.eudemon.ratelimiter.env.loader.ClassPathPropertySourceLoader;

import org.hamcrest.collection.IsMapContaining;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

@Test
public class ClassPathPropertySourceLoaderTest {

  public void testLoad() {
    ClassPathPropertySourceLoader loader = new ClassPathPropertySourceLoader();
    loader = Mockito.spy(loader);
    Mockito.when(loader.getAllMatchedConfigFiles()).thenReturn(
        new String[] {"classpath:ratelimiter-env.yaml", "classpath:no-existing.properties"});
    PropertySource propertySource = loader.load();
    Map<String, Object> properties = propertySource.getProperties();
    assertThat(properties, IsMapContaining.hasEntry("ratelimiter.rule.config.source", "file"));
    assertThat(properties, IsMapContaining.hasEntry("ratelimiter.rule.config.parser", "yaml"));
  }

}
