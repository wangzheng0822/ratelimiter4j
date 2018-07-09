package com.eudemon.ratelimiter.Interceptor;

import com.eudemon.ratelimiter.UrlRateLimiter;
import com.eudemon.ratelimiter.rule.ApiLimit;

/**
 * The interface for the different interceptors.
 */
public interface RateLimiterInterceptor {

  /**
   * This method will be called before {@link UrlRateLimiter#limit(String, String)}
   *
   * @param appId the app ID
   * @param api the API
   */
  void beforeLimit(String appId, String api);

  /**
   * This method will be called after {@link UrlRateLimiter#limit(String, String)}.
   *
   * @param appId the app ID
   * @param api the API
   * @param apiLimit contains all limit information, refer to {@link ApiLimit}
   * @param result true if {@link UrlRateLimiter#limit(String, String)} get an access token
   * @param ex the exception which is throwed by {@link UrlRateLimiter#limit(String, String)}
   */
  void afterLimit(String appId, String api, ApiLimit apiLimit, boolean result, Exception ex);

}
