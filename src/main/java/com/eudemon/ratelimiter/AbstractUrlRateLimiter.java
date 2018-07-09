package com.eudemon.ratelimiter;

import static com.eudemon.ratelimiter.context.RateLimiterBeansFactory.BEANS_CONTEXT;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eudemon.ratelimiter.Interceptor.RateLimiterInterceptor;
import com.eudemon.ratelimiter.Interceptor.RateLimiterInterceptorChain;
import com.eudemon.ratelimiter.algorithm.RateLimiter;
import com.eudemon.ratelimiter.exception.InternalErrorException;
import com.eudemon.ratelimiter.exception.InvalidUrlException;
import com.eudemon.ratelimiter.exception.OverloadException;
import com.eudemon.ratelimiter.rule.ApiLimit;
import com.eudemon.ratelimiter.rule.RateLimitRule;
import com.eudemon.ratelimiter.rule.source.RuleConfigSource;
import com.eudemon.ratelimiter.utils.UrlUtils;
import com.google.common.annotations.VisibleForTesting;

/**
 * Abstract class for url rate limit.
 */
public abstract class AbstractUrlRateLimiter implements UrlRateLimiter {

  private static final Logger logger = LoggerFactory.getLogger(AbstractUrlRateLimiter.class);

  private final ConcurrentHashMap<String, RateLimiter> counters = new ConcurrentHashMap<>(256);

  private final RateLimitRule rateLimitRule;

  private RateLimiterInterceptorChain interceptorChain;

  /**
   * Default construct.
   */
  public AbstractUrlRateLimiter() {
    this((RateLimitRule) null);
  }

  /**
   * Construct.
   * 
   * @param source the limit config source, if null, will use default rate limit config source.
   */
  public AbstractUrlRateLimiter(RuleConfigSource source) {
    this.rateLimitRule = BEANS_CONTEXT.obtainUrlRateLimitRule(null);
    /* load config from source and build rule. */
    source = BEANS_CONTEXT.obtainRuleConfigSource(source);
    this.rateLimitRule.addRule(source.load());

    this.interceptorChain = BEANS_CONTEXT.obtainInterceptorChain(null);
  }

  /**
   * Construct.
   * 
   * @param rule the limit rule, if null, rate limiter will load limit rule from file or zookeeper.
   */
  public AbstractUrlRateLimiter(RateLimitRule rule) {
    this(rule, null);
  }

  /**
   * Construct.
   * 
   * @param rule the limit rule, if null, rate limiter will load limit rule from file or zookeeper.
   * @param chain the interceptor chain, if null, will use default interceptor chain.
   */
  public AbstractUrlRateLimiter(RateLimitRule rule, RateLimiterInterceptorChain chain) {
    this.rateLimitRule = BEANS_CONTEXT.obtainUrlRateLimitRule(rule);
    if (rule == null) {
      /* load config from source and build rule. */
      RuleConfigSource source = BEANS_CONTEXT.obtainRuleConfigSource(null);
      this.rateLimitRule.addRule(source.load());
    }

    this.interceptorChain = BEANS_CONTEXT.obtainInterceptorChain(chain);
  }

  /**
   * Add interceptors into the default interceptor chain. The interceptor will do some work
   * before/after the {@code UrlRateLimiter.limit} method.
   * 
   * @param interceptors the interceptor list to be added into the interceptor chain.
   */
  @Override
  public void addInteceptors(List<RateLimiterInterceptor> interceptors) {
    if (interceptors != null && !interceptors.isEmpty()) {
      this.interceptorChain.addInterceptors(interceptors);
    }
  }

  /**
   * Add interceptor into the default interceptor chain. The interceptor will do some work
   * before/after the {@code UrlRateLimiter.limit} method.
   * 
   * @param interceptor the interceptor to be added into the interceptor chain.
   */
  @Override
  public void addInterceptor(RateLimiterInterceptor interceptor) {
    if (interceptor != null) {
      this.interceptorChain.addInterceptor(interceptor);
    }
  }

  /**
   * check if the url request of the specified app exceeds the max hit limit.
   * 
   * @param appId the app ID
   * @param url the request url.
   * @throws OverloadException if the app exceeds the max hit limit for the api.
   * @throws InvalidUrlException if the url is invalid.
   * @throws InternalErrorException if some internal error occurs.
   */
  @Override
  public void limit(String appId, String url)
      throws OverloadException, InvalidUrlException, InternalErrorException {
    interceptorChain.doBeforeLimit(appId, url);

    ApiLimit apiLimit = null;
    boolean passed = false;
    Exception exception = null;
    try {
      // TODO(zheng): validate url
      String urlPath = UrlUtils.getUrlPath(url);
      // TODO(zheng): do not need get apilimit every time.
      apiLimit = rateLimitRule.getLimit(appId, urlPath);
      if (apiLimit == null) {
        logger.warn("no rate limit rule for api: {}", urlPath);
        return; // passed
      }

      RateLimiter rateLimiter =
          getRateLimiterAlgorithm(appId, apiLimit.getApi(), apiLimit.getLimit());
      passed = rateLimiter.tryAcquire();
      if (!passed) {
        StringBuilder builder = new StringBuilder();
        builder.append(appId).append(":").append(url);
        builder.append(" has exceeded max tps limit:");
        builder.append(apiLimit.toString());
        throw new OverloadException(builder.toString());
      }
    } catch (OverloadException e) {
      passed = false;
      throw e;
    } catch ( InvalidUrlException | InternalErrorException e) {
      exception = e;
      throw e;
    } catch (Exception e) {
      InternalErrorException re = new InternalErrorException("Rate limiter internal error.", e);
      exception = re;
      throw re;
    } finally {
      interceptorChain.doAfterLimit(appId, url, apiLimit, passed, exception);
    }
  }

  /**
   * Create or get rate limit algorithm for every API.
   * 
   * @param appId the app ID
   * @param api the API
   * @param limit the max hit count limit per second.
   * @return the RateLimiter algorithm.
   * @throws InvalidUrlException if API is invalid.
   */
  @VisibleForTesting
  public RateLimiter getRateLimiterAlgorithm(String appId, String api, int limit)
      throws InvalidUrlException {
    String limitKey = generateUrlKey(appId, api);
    RateLimiter rateLimiter = counters.get(limitKey);
    if (rateLimiter == null) {
      RateLimiter newRateLimiter = createRateLimitAlgorithm(limitKey, limit);
      rateLimiter = counters.putIfAbsent(limitKey, newRateLimiter);
      if (rateLimiter == null) {
        rateLimiter = newRateLimiter;
      }
    }
    return rateLimiter;
  }

  /**
   * Create rate limiter algorithm.
   * 
   * @param limitKey the API key, such as "appid:api"
   * @param limit the max hit count limit per second.
   * @return the rate limit algorithm.
   */
  protected abstract RateLimiter createRateLimitAlgorithm(String limitKey, int limit);

  /**
   * Generate unique key for every appID and api pattern.
   * 
   * @param appId the app ID.
   * @param api the api pattern.
   * @return the unique key.
   */
  private String generateUrlKey(String appId, String api) {
    StringBuilder builder = new StringBuilder();
    builder.append(appId).append(":").append(api);
    return builder.toString();
  }

}
