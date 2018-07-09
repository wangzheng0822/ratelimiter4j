package com.eudemon.ratelimiter.env;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.eudemon.ratelimiter.redis.DefaultJedisPoolConfig;

/**
 * Configuration for Redis.
 * 
 * TODO(zheng): support redis cluster.
 */
public class RedisConfig {

  /* connectionTimeout and soTimeout */
  public static final int DEFAULT_TIMEOUT = 10; // 10ms

  /* default redis port */
  public static final int DEFAULT_PORT = 6379;

  private String address;

  private int timeout = DEFAULT_TIMEOUT;

  private GenericObjectPoolConfig poolConfig = new DefaultJedisPoolConfig();

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public GenericObjectPoolConfig getPoolConfig() {
    return poolConfig;
  }

  public void setPoolConfig(GenericObjectPoolConfig poolConfig) {
    this.poolConfig = poolConfig;
  }

  public void buildFromProperties(PropertySource propertySource) {
    if (propertySource == null) {
      return;
    }

    String addr = propertySource.getPropertyStringValue(PropertyConstants.PROPERTY_REDIS_ADDRESS);
    if (StringUtils.isNotBlank(addr)) {
      this.address = addr;
    }

    Integer timeout = propertySource.getPropertyIntValue(PropertyConstants.PROPERTY_REDIS_TIMEOUT);
    if (timeout != null) {
      this.timeout = timeout;
    }

    Integer maxTotal =
        propertySource.getPropertyIntValue(PropertyConstants.PROPERTY_REDIS_MAX_TOTAL);
    if (maxTotal != null) {
      this.poolConfig.setMaxTotal(maxTotal);
    }

    Integer maxIdle = propertySource.getPropertyIntValue(PropertyConstants.PROPERTY_REDIS_MAX_IDLE);
    if (maxIdle != null) {
      this.poolConfig.setMaxIdle(maxIdle);
    }

    Integer minIdle = propertySource.getPropertyIntValue(PropertyConstants.PROPERTY_REDIS_MIN_IDLE);
    if (minIdle != null) {
      this.poolConfig.setMinIdle(minIdle);
    }

    Integer maxWaitMillis =
        propertySource.getPropertyIntValue(PropertyConstants.PROPERTY_REDIS_MAX_WAIT_MILLIS);
    if (maxWaitMillis != null) {
      this.poolConfig.setMaxWaitMillis(maxWaitMillis);
    }

    Boolean testOnBorrow =
        propertySource.getPropertyBooleanValue(PropertyConstants.PROPERTY_REDIS_TEST_ON_BORROW);
    if (testOnBorrow != null) {
      this.poolConfig.setTestOnBorrow(testOnBorrow);
    }
  }

}
