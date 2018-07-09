package com.eudemon.ratelimiter.env.loader;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.eudemon.ratelimiter.env.PropertyConstants;
import com.eudemon.ratelimiter.env.PropertySource;
import com.eudemon.ratelimiter.extension.Order;

/**
 * This class loads environment configuration from JVM environment variables.
 */
@Order(Order.HIGHEST_PRECEDENCE + 10)
public class JvmPropertySourceLoader implements PropertySourceLoader {

  @Override
  public PropertySource load() {
    Properties properties = System.getProperties();
    Map<String, Object> ratelimiterProperties = new HashMap<String, Object>();
    Set<String> names = properties.stringPropertyNames();
    for (String name : names) {
      if (name.startsWith(PropertyConstants.PROPERTY_KEY_PREFIX)) {
        ratelimiterProperties.put(name, properties.get(name));
      }
    }
    PropertySource source = new PropertySource();
    source.addProperties(ratelimiterProperties);
    return source;
  }

}
