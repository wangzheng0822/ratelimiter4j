package com.eudemon.ratelimiter.env.loader;

import com.eudemon.ratelimiter.env.io.ResourceLoader;
import com.eudemon.ratelimiter.extension.Order;

/**
 * This class loads environment configuration from the specified file in class path.
 * TODO(zheng): support user specified file name.
 */
@Order(Order.HIGHEST_PRECEDENCE + 40)
public class ClassPathPropertySourceLoader extends AbstractFilePropertySourceLoader
    implements PropertySourceLoader {

  private static final String[] DEFAULT_CONFIG_FILES = new String[] {
      "classpath:ratelimiter-env.yaml",
      "classpath:ratelimiter-env.yml",
      "classpath:ratelimiter-env.properties"
  };

  public ClassPathPropertySourceLoader() {
    this(null);
  }

  public ClassPathPropertySourceLoader(ResourceLoader resourceLoader) {
    super(resourceLoader);
  }

  @Override
  public String[] getAllMatchedConfigFiles() {
    return DEFAULT_CONFIG_FILES;
  }

}
