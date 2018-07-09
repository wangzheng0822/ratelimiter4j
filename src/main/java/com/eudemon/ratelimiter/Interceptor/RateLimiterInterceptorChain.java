package com.eudemon.ratelimiter.Interceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.eudemon.ratelimiter.extension.OrderComparator;
import com.eudemon.ratelimiter.rule.ApiLimit;

/**
 * The interceptor chain which contains all installed interceptors.
 * This class is not thread-safe.
 */
public class RateLimiterInterceptorChain {

  private final List<RateLimiterInterceptor> interceptors;

  public RateLimiterInterceptorChain() {
    interceptors = new ArrayList<>();
  }

  public void doBeforeLimit(String appId, String api) {
    for (int i = 0; i < interceptors.size(); i++) {
      interceptors.get(i).beforeLimit(appId, api);
    }
  }

  public void doAfterLimit(String appId, String url, ApiLimit apiLimit, boolean result,
      Exception ex) {
    for (int i = interceptors.size() - 1; i >= 0; i--) {
      interceptors.get(i).afterLimit(appId, url, apiLimit, result, ex);
    }
  }

  public List<RateLimiterInterceptor> getInterceptors() {
    return interceptors;
  }

  public void addInterceptor(RateLimiterInterceptor interceptor) {
    this.interceptors.add(interceptor);
    Collections.sort(this.interceptors, OrderComparator.INSTANCE);
  }

  public void addInterceptors(Collection<RateLimiterInterceptor> interceptors) {
    this.interceptors.addAll(interceptors);
    Collections.sort(this.interceptors, OrderComparator.INSTANCE);
  }

  public void clear() {
    this.interceptors.clear();
  }

  public boolean isEmpty() {
    return interceptors == null || interceptors.size() == 0;
  }

}
