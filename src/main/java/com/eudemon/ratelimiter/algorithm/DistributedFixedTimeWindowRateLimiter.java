package com.eudemon.ratelimiter.algorithm;

import static com.eudemon.ratelimiter.utils.SHA1.sha1Hex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eudemon.ratelimiter.exception.InternalErrorException;
import com.eudemon.ratelimiter.redis.JedisTaskExecutor;

import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.exceptions.JedisNoScriptException;

/**
 * This class implements distributed fixed time window rate limiter algorithm base on Redis.
 * This class is thread-safe.
 */
public class DistributedFixedTimeWindowRateLimiter implements RateLimiter {

  private static final Logger log = LoggerFactory.getLogger(DistributedFixedTimeWindowRateLimiter.class);

  /* the target key to be rate-limited */
  private final String key;

  /* the max permitted access count per second */
  private final int limit;

  /* the Jedis wrapper to access Redis. */
  private JedisTaskExecutor jedisTaskExecutor;

  /* TPS:limit/1s KEYS[1]=key,ARGV[1]=limit,return=result */
  public static final String REDIS_LIMIT_SCRIPT =
      "local key = KEYS[1] " +
      "local limit = tonumber(ARGV[1]) " +
      "local current = tonumber(redis.call('incr', key)) " +
      "if current > limit then " +
      "   return 0 " +
      "elseif current == 1 then " +
      "   redis.call('expire', key, '1') " +
      "end " +
      "return 1 ";

  /* Redis cache for Lua script. */
  public static final String REDIS_LIMIT_SCRIPT_SHA1 = sha1Hex(REDIS_LIMIT_SCRIPT);

  /**
   * Construct.
   * 
   * @param key the target key to be rate-limited
   * @param limit the rate limit count.
   * @param jedisTaskExecutor the jedis wrapper to access to Redis.
   */
  public DistributedFixedTimeWindowRateLimiter(String key, int limit, JedisTaskExecutor jedisTaskExecutor) {
    this.key = key;
    this.limit = limit;
    this.jedisTaskExecutor = jedisTaskExecutor;
  }

  /**
   * try to acquire an access token.
   * TODO(zheng): handle timeout exception separately!
   * 
   * @return true if get an access token successfully, otherwise, return false.
   * @throws InternalErrorException if failed to access Redis.
   */
  @Override
  public boolean tryAcquire() throws InternalErrorException {
    long result = 0;
    try {
      result = (long) jedisTaskExecutor.evalsha(REDIS_LIMIT_SCRIPT_SHA1, key,
          String.valueOf(limit));
      return 1 == result;
    } catch (JedisNoScriptException e) {
      log.warn("no lua script cache on redis server.", e);
    } catch (JedisConnectionException e) {
      throw new InternalErrorException("Read redis error.", e);
    } catch (JedisException e) {
      throw new InternalErrorException("Read redis error.", e);
    }

    try {
      result = (long) jedisTaskExecutor.eval(REDIS_LIMIT_SCRIPT, key, String.valueOf(limit));
    } catch (JedisConnectionException ee) {
      throw new InternalErrorException("Read redis error.", ee);
    }

    return 1 == result;
  }

}
