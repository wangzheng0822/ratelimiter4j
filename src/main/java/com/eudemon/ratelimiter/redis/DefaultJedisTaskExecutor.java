package com.eudemon.ratelimiter.redis;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.eudemon.ratelimiter.env.RedisConfig;
import com.google.common.collect.Lists;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Default implementation for {@link JedisTaskExecutor} which is the Jedis wrapper used to access
 * Redis.
 * 
 * TODO(zheng): support redis cluster.
 */
public class DefaultJedisTaskExecutor implements JedisTaskExecutor {

  public static final String DEFAULT_REDIS_KEY_PREFIX = "rt:";

  private JedisPool pool;

  private String redisKeyPrefix = DEFAULT_REDIS_KEY_PREFIX;

  public DefaultJedisTaskExecutor(JedisPool pool) {
    this(pool, DEFAULT_REDIS_KEY_PREFIX);
  }

  public DefaultJedisTaskExecutor(JedisPool pool, String prefix) {
    this.pool = pool;
    this.redisKeyPrefix = prefix;
  }

  public DefaultJedisTaskExecutor(String address, int timeout, GenericObjectPoolConfig poolConfig) {
    this(address, timeout, poolConfig, DEFAULT_REDIS_KEY_PREFIX);
  }

  public DefaultJedisTaskExecutor(String address, int timeout, GenericObjectPoolConfig poolConfig,
      String prefix) {
    if (StringUtils.isBlank(address)) {
      throw new RuntimeException("redis address is empty.");
    }

    String[] ipAndPort = address.split(":");
    String host = ipAndPort[0];
    int port = RedisConfig.DEFAULT_PORT;
    if (ipAndPort.length >= 2) {
      try {
        port = Integer.parseInt(ipAndPort[1]);
      } catch (NumberFormatException e) {
        port = RedisConfig.DEFAULT_PORT;
      }
    }
    if (poolConfig == null) {
      poolConfig = new DefaultJedisPoolConfig();
    }
    this.pool = new JedisPool(poolConfig, host, port, timeout);
    this.redisKeyPrefix = prefix;
  }

  @Override
  public Object eval(final String luaScript) {
    return execute((jedis) -> jedis.eval(luaScript));
  }

  @Override
  public Object eval(String luaScript, String key, String params) {
    return execute((jedis) -> jedis.eval(luaScript, Lists.newArrayList(redisKeyPrefix + key),
        Lists.newArrayList(params)));
  }

  @Override
  public Object evalsha(final String sha1, final String key, final String params) {
    return execute((jedis) -> jedis.evalsha(sha1, Lists.newArrayList(redisKeyPrefix + key),
        Lists.newArrayList(params)));
  }

  @Override
  public String set(String key, String value) {
    return execute((jedis -> jedis.set(redisKeyPrefix + key, value)));
  }

  private <T> T execute(JedisTask<T> task) {
    T result;
    try (Jedis jedis = pool.getResource()) {
      result = task.run(jedis);
    }
    return result;
  }

}
