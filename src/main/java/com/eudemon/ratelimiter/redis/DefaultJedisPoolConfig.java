package com.eudemon.ratelimiter.redis;

import redis.clients.jedis.JedisPoolConfig;

/**
 * Default Jedis pool configuration.
 */
public class DefaultJedisPoolConfig extends JedisPoolConfig {

  private static final int DEFAULT_MAX_TOTAL = 50;
  private static final int DEFAULT_MAX_IDLE = 50;
  private static final int DEFAULT_MIN_IDLE = 20;
  private static final long DEFAULT_MAX_WAIT_MILLIS = 10;
  private static final boolean DEFAULT_TEST_ON_BORROW = true;

  public DefaultJedisPoolConfig() {
    super();
    setMaxTotal(DEFAULT_MAX_TOTAL);
    setMaxIdle(DEFAULT_MAX_IDLE);
    setMinIdle(DEFAULT_MIN_IDLE);
    setMaxWaitMillis(DEFAULT_MAX_WAIT_MILLIS);
    setTestOnBorrow(DEFAULT_TEST_ON_BORROW);
  }

}
