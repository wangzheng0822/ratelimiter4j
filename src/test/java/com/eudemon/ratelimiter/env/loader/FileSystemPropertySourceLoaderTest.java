package com.eudemon.ratelimiter.env.loader;

import com.eudemon.ratelimiter.env.PropertySource;
import com.eudemon.ratelimiter.env.loader.FileSystemPropertySourceLoader;

import org.hamcrest.collection.IsMapContaining;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

@Test
public class FileSystemPropertySourceLoaderTest {

  public void testLoad() {
    FileSystemPropertySourceLoader loader = new FileSystemPropertySourceLoader();
    loader = Mockito.spy(loader);
    Mockito.when(loader.getAllMatchedConfigFiles()).thenReturn(new String[] {
        "src/test/resources/ratelimiter-env.yaml", "src/test/resource/no-existing.properties"});
    PropertySource propertySource = loader.load();
    Map<String, Object> properties = propertySource.getProperties();
    assertThat(properties, IsMapContaining.hasEntry("ratelimiter.rule.config.source", "file"));
    assertThat(properties, IsMapContaining.hasEntry("ratelimiter.rule.config.parser", "yaml"));
  }

}
