package com.eudemon.ratelimiter.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

import com.eudemon.ratelimiter.env.loader.ClassPathPropertySourceLoader;
import com.eudemon.ratelimiter.env.loader.FileSystemPropertySourceLoader;
import com.eudemon.ratelimiter.env.loader.JvmPropertySourceLoader;
import com.eudemon.ratelimiter.env.loader.PropertySourceLoader;
import com.eudemon.ratelimiter.env.loader.SystemPropertySourceLoader;
import com.eudemon.ratelimiter.extension.OrderComparator;
import com.google.common.annotations.VisibleForTesting;

/**
 * The class is a singleton class. 
 * The class contains all environment configuration for rate
 * limiter.
 *
 * TODO(zheng): add method void reload();
 */
public class RateLimiterConfig {

  private List<PropertySourceLoader> sourceLoaders;

  private RedisConfig redisConfig = new RedisConfig();

  private ZookeeperConfig zookeeperConfig = new ZookeeperConfig();

  // TODO(zheng): make it ENUM
  private String ruleConfigParserType = "yaml"; // yaml or json, default yaml

  // TODO(zheng): make it ENUM
  private String ruleConfigSourceType = "file"; // zookeeper or file, default file

  private AtomicBoolean isInitialized = new AtomicBoolean(false);

  private static final class RateLimiterConfigHolder {
    public static final RateLimiterConfig INSTANCE = new RateLimiterConfig();
  }

  public static final RateLimiterConfig instance() {
    return RateLimiterConfigHolder.INSTANCE;
  }

  private RateLimiterConfig() {
    // TODO(zheng): refactor it using factories.
    sourceLoaders = new ArrayList<PropertySourceLoader>();
    sourceLoaders.add(new ClassPathPropertySourceLoader());
    sourceLoaders.add(new FileSystemPropertySourceLoader());
    sourceLoaders.add(new SystemPropertySourceLoader());
    sourceLoaders.add(new JvmPropertySourceLoader());
  }

  @VisibleForTesting
  protected RateLimiterConfig(List<PropertySourceLoader> propertySourceLoaders) {
    this.sourceLoaders = propertySourceLoaders;
  }

  /**
   * load( or override) priority: jvm &gt; system &gt; file(yaml, properties) &gt; classpath(yaml,
   * properties)
   *
   * TODO(zheng): support more environment source command line &gt; jvm &gt; system &gt; jndi &gt;
   * file &gt; classpath
   */
  public void load() {
    if (!isInitialized.compareAndSet(false, true)) {
      return;
    }

    /* load data from list of PropertySourceLoaders */
    PropertySource propertySource = new PropertySource();
    Collections.sort(sourceLoaders, OrderComparator.INSTANCE);
    for (int i = sourceLoaders.size() - 1; i >= 0; --i) {
      PropertySourceLoader loader = sourceLoaders.get(i);
      if (loader != null) {
        propertySource.combinePropertySource(loader.load());
      }
    }

    mapPropertiesToConfigs(propertySource);
  }

  public String getRuleConfigParserType() {
    return ruleConfigParserType;
  }

  public void setRuleConfigParserType(String ruleConfigParserType) {
    this.ruleConfigParserType = ruleConfigParserType;
  }

  public String getRuleConfigSourceType() {
    return ruleConfigSourceType;
  }

  public void setRuleConfigSourceType(String ruleConfigSourceType) {
    this.ruleConfigSourceType = ruleConfigSourceType;
  }

  public RedisConfig getRedisConfig() {
    return this.redisConfig;
  }

  public void setRedisConfig(RedisConfig redisConfig) {
    this.redisConfig = redisConfig;
  }

  public ZookeeperConfig getZookeeperConfig() {
    return zookeeperConfig;
  }

  public void setZookeeperConfig(ZookeeperConfig zookeeperConfig) {
    this.zookeeperConfig = zookeeperConfig;
  }

  private void mapPropertiesToConfigs(PropertySource propertySource) {
    String parserType =
        propertySource.getPropertyStringValue(PropertyConstants.PROPERTY_RULE_CONFIG_PARSER);
    if (StringUtils.isNotBlank(parserType)) {
      this.ruleConfigParserType = parserType;
    }

    String source =
        propertySource.getPropertyStringValue(PropertyConstants.PROPERTY_RULE_CONFIG_SOURCE);
    if (StringUtils.isNotBlank(source)) {
      this.ruleConfigSourceType = source;
    }

    redisConfig.buildFromProperties(propertySource);
    zookeeperConfig.buildFromProperties(propertySource);
  }

}
