package com.eudemon.ratelimiter.env.loader;

import java.util.HashMap;
import java.util.Map;

import com.eudemon.ratelimiter.env.PropertyConstants;
import com.eudemon.ratelimiter.env.PropertySource;
import com.eudemon.ratelimiter.extension.Order;

/**
 * This class loads environment configuration from system environment variables.
 */
@Order(Order.HIGHEST_PRECEDENCE + 20)
public class SystemPropertySourceLoader implements PropertySourceLoader {

  @Override
  public PropertySource load() {
    Map<String, Object> ratelimiterProperties = new HashMap<String, Object>();
    Map<String, String> envs = getEnv();
    for (Map.Entry<String, String> env : envs.entrySet()) {
      if (env.getKey().startsWith(PropertyConstants.PROPERTY_KEY_PREFIX)) {
        ratelimiterProperties.put(env.getKey(), env.getValue());
      }
    }
    PropertySource source = new PropertySource();
    source.addProperties(ratelimiterProperties);
    return source;
  }

  protected Map<String, String> getEnv() {
    return System.getenv();
  }

}
