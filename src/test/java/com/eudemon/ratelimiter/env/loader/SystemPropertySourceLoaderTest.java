package com.eudemon.ratelimiter.env.loader;

import com.eudemon.ratelimiter.env.PropertySource;
import com.eudemon.ratelimiter.env.loader.SystemPropertySourceLoader;

import org.hamcrest.collection.IsMapContaining;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertNull;

@Test
public class SystemPropertySourceLoaderTest {

  public void testLoad() {
    Map<String, String> preSetValues = new HashMap<>();
    preSetValues.put("ratelimiter.rule.source", "zookeeper");
    preSetValues.put("ratelimiter.rule.parser", "yaml");
    preSetValues.put("ratelimiter.redis.ip", "12.34.22.123");

    SystemPropertySourceLoader loader = new SystemPropertySourceLoader();
    loader = Mockito.spy(loader);
    Mockito.when(loader.getEnv()).thenReturn(preSetValues);
    PropertySource propertySource = loader.load();
    Map<String, Object> properties = propertySource.getProperties();

    for (Map.Entry<String, String> v : preSetValues.entrySet()) {
      assertThat(properties, IsMapContaining.hasEntry(v.getKey(), v.getValue()));
    }

    assertNull(properties.get("invalid-key"));
  }

}
