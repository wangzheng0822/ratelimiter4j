package com.eudemon.ratelimiter;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.eudemon.ratelimiter.Interceptor.RateLimiterInterceptor;
import com.eudemon.ratelimiter.Interceptor.RateLimiterInterceptorChain;
import com.eudemon.ratelimiter.algorithm.FixedTimeWindowRateLimiter;
import com.eudemon.ratelimiter.algorithm.RateLimiter;
import com.eudemon.ratelimiter.env.ZookeeperConfig;
import com.eudemon.ratelimiter.rule.RateLimitRule;
import com.eudemon.ratelimiter.rule.parser.JsonRuleConfigParser;
import com.eudemon.ratelimiter.rule.parser.RuleConfigParser;
import com.eudemon.ratelimiter.rule.parser.YamlRuleConfigParser;
import com.eudemon.ratelimiter.rule.source.FileRuleConfigSource;
import com.eudemon.ratelimiter.rule.source.RuleConfigSource;
import com.eudemon.ratelimiter.rule.source.ZookeeperRuleConfigSource;

/**
 * Fixed time window rate limiter based on memory. This class is thread safe.
 */
public class MemoryUrlRateLimiter extends AbstractUrlRateLimiter implements UrlRateLimiter {

  /**
   * Default construct.
   */
  public MemoryUrlRateLimiter() {
    this((RuleConfigSource) null);
  }

  /**
   * Construct.
   * 
   * @param source the limit config source, if null, will use default rate limit config source.
   */
  public MemoryUrlRateLimiter(RuleConfigSource source) {
    super(source);
  }

  /**
   * Construct.
   * 
   * @param rule the limit rule, if null, rate limiter will load limit rule from file or zookeeper.
   */
  public MemoryUrlRateLimiter(RateLimitRule rule) {
    super(rule);
  }

  /**
   * Construct.
   * 
   * @param rule the limit rule, if null, rate limiter will load limit rule from file or zookeeper.
   */
  public MemoryUrlRateLimiter(RateLimitRule rule, RateLimiterInterceptorChain chain) {
    super(rule, chain);
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
    return new FixedTimeWindowRateLimiter(limit);
  }

  public static MemoryUrlRateLimiterbuilder builder = new MemoryUrlRateLimiterbuilder();

  public static class MemoryUrlRateLimiterbuilder {

    /* zookeeper configuration */
    private ZookeeperConfig zookeeperConfig;

    /* interceptors */
    private List<RateLimiterInterceptor> interceptors;

    /* rule configuration parser: yaml or json */
    private String ruleParserType = "yaml";

    /* source type: file or zookeeper */
    private String ruleSourceType = "file";

    public MemoryUrlRateLimiterbuilder() {}

    public MemoryUrlRateLimiterbuilder setZookeeperConfig(ZookeeperConfig zookeeperConfig) {
      this.zookeeperConfig = zookeeperConfig;
      return this;
    }

    public MemoryUrlRateLimiterbuilder setInterceptors(List<RateLimiterInterceptor> interceptors) {
      this.interceptors = interceptors;
      return this;
    }

    public MemoryUrlRateLimiterbuilder setRuleParserType(String ruleParserType) {
      if (StringUtils.isNotBlank(ruleParserType)) {
        this.ruleParserType = ruleParserType;
      }
      return this;
    }

    public MemoryUrlRateLimiterbuilder setRuleSourceType(String ruleSourceType) {
      if (StringUtils.isNotBlank(ruleSourceType)) {
        this.ruleSourceType = ruleSourceType;
      }
      return this;
    }

    public MemoryUrlRateLimiter build() {
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

      MemoryUrlRateLimiter ratelimiter = new MemoryUrlRateLimiter(source);
      if (this.interceptors != null && !this.interceptors.isEmpty()) {
        ratelimiter.addInteceptors(interceptors);
      }
      return ratelimiter;
    }

  }

}
