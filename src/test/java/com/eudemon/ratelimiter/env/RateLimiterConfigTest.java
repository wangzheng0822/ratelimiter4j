package com.eudemon.ratelimiter.env;

import com.eudemon.ratelimiter.env.PropertyConstants;
import com.eudemon.ratelimiter.env.PropertySource;
import com.eudemon.ratelimiter.env.RateLimiterConfig;
import com.eudemon.ratelimiter.env.loader.PropertySourceLoader;
import com.eudemon.ratelimiter.extension.Order;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

@Test
public class RateLimiterConfigTest {

  public void testLoad() {
    List<PropertySourceLoader> loaders = new ArrayList<>();
    loaders.add(new PropertySourceLoaderB());
    loaders.add(new PropertySourceLoaderA());
    loaders.add(new PropertySourceLoaderC());

    RateLimiterConfig config = new RateLimiterConfig(loaders);
    config.load();

    assertEquals(config.getRuleConfigSourceType(), "C-source");
    assertEquals(config.getRuleConfigParserType(), "C-parser");
    assertEquals(config.getRedisConfig().getAddress(), "A-addr");
  }

  @Order(Order.HIGHEST_PRECEDENCE + 30)
  public static final class PropertySourceLoaderA implements PropertySourceLoader {

    @Override
    public PropertySource load() {
      PropertySource propertySource = new PropertySource();
      Map<String, Object> properties = new LinkedHashMap<>();
      properties.put(PropertyConstants.PROPERTY_RULE_CONFIG_SOURCE, "A-source");
      properties.put(PropertyConstants.PROPERTY_RULE_CONFIG_PARSER, "A-parser");
      properties.put(PropertyConstants.PROPERTY_REDIS_ADDRESS, "A-addr");
      propertySource.addProperties(properties);
      return propertySource;
    }

  }

  @Order(Order.HIGHEST_PRECEDENCE + 20)
  public static final class PropertySourceLoaderB implements PropertySourceLoader {

    @Override
    public PropertySource load() {
      PropertySource propertySource = new PropertySource();
      Map<String, Object> properties = new LinkedHashMap<>();
      properties.put(PropertyConstants.PROPERTY_RULE_CONFIG_SOURCE, "B-source");
      propertySource.addProperties(properties);
      return propertySource;
    }

  }

  @Order(Order.HIGHEST_PRECEDENCE + 10)
  public static final class PropertySourceLoaderC implements PropertySourceLoader {

    @Override
    public PropertySource load() {
      PropertySource propertySource = new PropertySource();
      Map<String, Object> properties = new LinkedHashMap<>();
      properties.put(PropertyConstants.PROPERTY_RULE_CONFIG_SOURCE, "C-source");
      properties.put(PropertyConstants.PROPERTY_RULE_CONFIG_PARSER, "C-parser");
      propertySource.addProperties(properties);
      return propertySource;
    }

  }

}
