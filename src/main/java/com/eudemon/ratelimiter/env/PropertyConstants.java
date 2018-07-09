package com.eudemon.ratelimiter.env;

/**
 * Property name constants for environment configuration.
 */
public class PropertyConstants {

  public static final String PROPERTY_KEY_PREFIX = "ratelimiter";

  /* rule format: yaml, json, default is yaml.*/
  public static final String PROPERTY_RULE_CONFIG_PARSER =
      PROPERTY_KEY_PREFIX + ".rule.config.parser";
  
  /* rule configuration source type: file or zookeeper, default is file.*/
  public static final String PROPERTY_RULE_CONFIG_SOURCE =
      PROPERTY_KEY_PREFIX + ".rule.config.source";

  /* Redis config if needed. */
  public static final String PROPERTY_REDIS_ADDRESS = PROPERTY_KEY_PREFIX + ".redis.address";
  public static final String PROPERTY_REDIS_MAX_TOTAL = PROPERTY_KEY_PREFIX + ".redis.maxTotal";
  public static final String PROPERTY_REDIS_MAX_IDLE = PROPERTY_KEY_PREFIX + ".redis.maxIdle";
  public static final String PROPERTY_REDIS_MIN_IDLE = PROPERTY_KEY_PREFIX + ".redis.minIdle";
  public static final String PROPERTY_REDIS_MAX_WAIT_MILLIS =
      PROPERTY_KEY_PREFIX + ".redis.maxWaitMillis";
  public static final String PROPERTY_REDIS_TEST_ON_BORROW =
      PROPERTY_KEY_PREFIX + ".redis.testOnBorrow";
  public static final String PROPERTY_REDIS_TIMEOUT = PROPERTY_KEY_PREFIX + ".redis.timeout";

  /* Zookeeper config if needed. */
  public static final String PROPERTY_ZOOKEEPER_ADDRESS =
      PROPERTY_KEY_PREFIX + ".zookeeper.address";
  public static final String PROPERTY_ZOOKEEPER_RULE_PATH =
      PROPERTY_KEY_PREFIX + ".zookeeper.rule.path";
}
