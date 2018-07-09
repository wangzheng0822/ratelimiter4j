package com.eudemon.ratelimiter;

import static com.eudemon.ratelimiter.context.RateLimiterBeansFactory.BEANS_CONTEXT;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.eudemon.ratelimiter.Interceptor.RateLimiterInterceptor;
import com.eudemon.ratelimiter.Interceptor.RateLimiterInterceptorChain;
import com.eudemon.ratelimiter.algorithm.DistributedFixedTimeWindowRateLimiter;
import com.eudemon.ratelimiter.algorithm.RateLimiter;
import com.eudemon.ratelimiter.env.RedisConfig;
import com.eudemon.ratelimiter.env.ZookeeperConfig;
import com.eudemon.ratelimiter.redis.DefaultJedisTaskExecutor;
import com.eudemon.ratelimiter.redis.JedisTaskExecutor;
import com.eudemon.ratelimiter.rule.RateLimitRule;
import com.eudemon.ratelimiter.rule.parser.JsonRuleConfigParser;
import com.eudemon.ratelimiter.rule.parser.RuleConfigParser;
import com.eudemon.ratelimiter.rule.parser.YamlRuleConfigParser;
import com.eudemon.ratelimiter.rule.source.FileRuleConfigSource;
import com.eudemon.ratelimiter.rule.source.RuleConfigSource;
import com.eudemon.ratelimiter.rule.source.ZookeeperRuleConfigSource;

/**
 * Fixed time window rate limiter based on Redis. This class is thread safe.
 */
public class DistributedUrlRateLimiter extends AbstractUrlRateLimiter implements UrlRateLimiter {

  /* The Jedis wrapper to access Redis. */
  private JedisTaskExecutor jedisTaskExecutor;

  /**
   * Default construct.
   */
  public DistributedUrlRateLimiter() {
    this(null, (RateLimitRule) null);
  }

  /**
   * Construct.
   * 
   * @param jedisTaskExecutor the Jedis wrapper to access Redis.
   */
  public DistributedUrlRateLimiter(JedisTaskExecutor jedisTaskExecutor) {
    this(jedisTaskExecutor, (RateLimitRule) null);
  }

  /**
   * Construct.
   * 
   * @param jedisTaskExecutor the Jedis wrapper to access Redis.
   * @param source the limit config source, if null, will use default rate limit config source.
   */
  public DistributedUrlRateLimiter(JedisTaskExecutor jedisTaskExecutor, RuleConfigSource source) {
    super(source);
    this.jedisTaskExecutor = BEANS_CONTEXT.obtainJedisTaskExecutor(jedisTaskExecutor);
  }

  /**
   * Construct.
   * 
   * @param jedisTaskExecutor the Jedis wrapper to access Redis.
   * @param rule the limit rule, if null, rate limiter will load limit rule from file or zookeeper.
   */
  public DistributedUrlRateLimiter(JedisTaskExecutor jedisTaskExecutor, RateLimitRule rule) {
    super(rule);
    this.jedisTaskExecutor = BEANS_CONTEXT.obtainJedisTaskExecutor(jedisTaskExecutor);
  }

  /**
   * Construct.
   * 
   * @param jedisTaskExecutor the Jedis wrapper to access Redis.
   * @param rule the limit rule, if null, rate limiter will load limit rule from file or zookeeper.
   * @param chain the interceptor chain, if null, will use default interceptor chain.
   */
  public DistributedUrlRateLimiter(JedisTaskExecutor jedisTaskExecutor, RateLimitRule rule,
      RateLimiterInterceptorChain chain) {
    super(rule, chain);
    this.jedisTaskExecutor = BEANS_CONTEXT.obtainJedisTaskExecutor(jedisTaskExecutor);
  }

  /**
   * Create rate limiter algorithm.
   * 
   * @param limitKey the API key, such as "appid:api"
   * @param limit the max hit count limit per second.
   * @return the rate limit algorithm.
   */
  @Override
  protected RateLimiter createRateLimitAlgorithm(String limitKey, int limit) {
    // TODO(zheng): get rate limit algorithm according to config.
    return new DistributedFixedTimeWindowRateLimiter(limitKey, limit, jedisTaskExecutor);
  }

  public static DistributedUrlRateLimiterbuilder builder = new DistributedUrlRateLimiterbuilder();

  public static class DistributedUrlRateLimiterbuilder {
    /* redis configuratoin */
    private RedisConfig redisConfig;

    /* zookeeper configuration */
    private ZookeeperConfig zookeeperConfig;

    /* interceptors */
    private List<RateLimiterInterceptor> interceptors;

    /* rule configuration parser: yaml or json */
    private String ruleParserType = "yaml";

    /* source type: file or zookeeper */
    private String ruleSourceType = "file";

    public DistributedUrlRateLimiterbuilder() {}

    public DistributedUrlRateLimiterbuilder setRedisConfig(RedisConfig redisConfig) {
      this.redisConfig = redisConfig;
      return this;
    }

    public DistributedUrlRateLimiterbuilder setZookeeperConfig(ZookeeperConfig zookeeperConfig) {
      this.zookeeperConfig = zookeeperConfig;
      return this;
    }

    public DistributedUrlRateLimiterbuilder setInterceptors(
        List<RateLimiterInterceptor> interceptors) {
      this.interceptors = interceptors;
      return this;
    }

    public DistributedUrlRateLimiterbuilder setRuleParserType(String ruleParserType) {
      if (StringUtils.isNotBlank(ruleParserType)) {
        this.ruleParserType = ruleParserType;
      }
      return this;
    }

    public DistributedUrlRateLimiterbuilder setRuleSourceType(String ruleSourceType) {
      if (StringUtils.isNotBlank(ruleSourceType)) {
        this.ruleSourceType = ruleSourceType;
      }
      return this;
    }

    public DistributedUrlRateLimiter build() {
      JedisTaskExecutor executor = new DefaultJedisTaskExecutor(redisConfig.getAddress(),
          redisConfig.getTimeout(), redisConfig.getPoolConfig());

      RuleConfigParser parser = null;
      if (this.ruleParserType.equals("yaml")) {
        parser = new YamlRuleConfigParser();
      } else if (this.ruleParserType.equals("json")) {
        parser = new JsonRuleConfigParser();
      } else {
        throw new RuntimeException("Do not support the rule paser type: " + this.ruleParserType);
      }

      RuleConfigSource source = null;
      if (this.ruleSourceType.equals("file")) {
        source = new FileRuleConfigSource();
      } else if (this.ruleSourceType.equals("zookeeper")) {
        if (zookeeperConfig != null && StringUtils.isNoneBlank(zookeeperConfig.getAddress())
            && StringUtils.isNoneBlank(zookeeperConfig.getPath())) {
          source = new ZookeeperRuleConfigSource(zookeeperConfig.getAddress(),
              zookeeperConfig.getPath(), parser);
        } else {
          throw new RuntimeException("some zookeeper configuration is empty.");
        }
      } else {
        throw new RuntimeException("Do not support the rule source type: " + this.ruleSourceType);
      }

      DistributedUrlRateLimiter ratelimiter = new DistributedUrlRateLimiter(executor, source);
      if (this.interceptors != null && !this.interceptors.isEmpty()) {
        ratelimiter.addInteceptors(interceptors);
      }
      return ratelimiter;
    }

  }

}
