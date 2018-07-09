package com.eudemon.ratelimiter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.eudemon.ratelimiter.AbstractUrlRateLimiter;
import com.eudemon.ratelimiter.Interceptor.RateLimiterInterceptorChain;
import com.eudemon.ratelimiter.algorithm.RateLimiter;
import com.eudemon.ratelimiter.exception.InternalErrorException;
import com.eudemon.ratelimiter.exception.OverloadException;
import com.eudemon.ratelimiter.rule.ApiLimit;
import com.eudemon.ratelimiter.rule.RateLimitRule;

@Test
public class UrlRateLimiterTest {

  public void testLimit() throws Exception {
    // rule
    RateLimitRule rule = Mockito.mock(RateLimitRule.class);
    when(rule.getLimit(any(), any())).thenReturn(new ApiLimit("/rt", 10));

    // chain
    RateLimiterInterceptorChain chain = Mockito.mock(RateLimiterInterceptorChain.class);

    // rate limit algorithm
    RateLimiter ratelimiter = Mockito.mock(RateLimiter.class);
    AbstractUrlRateLimiter urlRateLimiter = new AbstractUrlRateLimiter(rule, chain) {

      @Override
      protected RateLimiter createRateLimitAlgorithm(String limitKey, int limit) {
        return ratelimiter;
      }

    };

    // passed
    when(ratelimiter.tryAcquire()).thenReturn(true);
    try {
      urlRateLimiter.limit("appid", "http://www.eudemon.com/rt?key1=1&key2=2");
    } catch (Exception e) {
      fail("should not throw exception.");
    } finally {
      verify(chain, times(1)).doBeforeLimit(any(), any());
      verify(chain, times(1)).doAfterLimit(any(), any(), any(), eq(true), eq(null));
    }
  }

  public void testLimit_withoutConfiguredRateLimit() throws Exception {
    // rule
    RateLimitRule rule = Mockito.mock(RateLimitRule.class);
    when(rule.getLimit(any(), any())).thenReturn(null);

    // chain
    RateLimiterInterceptorChain chain = Mockito.mock(RateLimiterInterceptorChain.class);

    AbstractUrlRateLimiter urlRateLimiter = new AbstractUrlRateLimiter(rule, chain) {

      @Override
      protected RateLimiter createRateLimitAlgorithm(String limitKey, int limit) {
        return null;
      }

    };

    try {
      urlRateLimiter.limit("appid", "http://www.eudemon.com/rt?key1=1&key2=2");
    } catch (Exception e) {
      fail("should not throw exception.");
    } finally {
      verify(chain, times(1)).doBeforeLimit(any(), any());
      verify(chain, times(1)).doAfterLimit(any(), any(), any(), eq(false), eq(null));
    }
  }

  @Test(expectedExceptions = {OverloadException.class})
  public void testLimit_overload() throws Exception {
    // rule
    RateLimitRule rule = Mockito.mock(RateLimitRule.class);
    when(rule.getLimit(any(), any())).thenReturn(new ApiLimit("/rt", 10));

    // chain
    RateLimiterInterceptorChain chain = Mockito.mock(RateLimiterInterceptorChain.class);

    // rate limit algorithm
    RateLimiter ratelimiter = Mockito.mock(RateLimiter.class);
    AbstractUrlRateLimiter urlRateLimiter = new AbstractUrlRateLimiter(rule, chain) {

      @Override
      protected RateLimiter createRateLimitAlgorithm(String limitKey, int limit) {
        return ratelimiter;
      }

    };

    // passed
    when(ratelimiter.tryAcquire()).thenReturn(false);
    try {
      urlRateLimiter.limit("appid", "http://www.eudemon.com/rt?key1=1&key2=2");
    } finally {
      verify(chain, times(1)).doBeforeLimit(any(), any());
      verify(chain, times(1)).doAfterLimit(any(), any(), any(), eq(false), eq(null));
    }
  }

  public void testLimit_invalidUrl() {
    // TODO
  }

  @Test(expectedExceptions = {InternalErrorException.class})
  public void testLimit_internalerror() throws Exception {
    // rule
    RateLimitRule rule = Mockito.mock(RateLimitRule.class);
    when(rule.getLimit(any(), any())).thenReturn(new ApiLimit("/rt", 10));

    // chain
    RateLimiterInterceptorChain chain = Mockito.mock(RateLimiterInterceptorChain.class);

    // rate limit algorithm
    RateLimiter ratelimiter = Mockito.mock(RateLimiter.class);
    AbstractUrlRateLimiter urlRateLimiter = new AbstractUrlRateLimiter(rule, chain) {

      @Override
      protected RateLimiter createRateLimitAlgorithm(String limitKey, int limit) {
        return ratelimiter;
      }

    };

    // passed
    when(ratelimiter.tryAcquire()).thenThrow(new InternalErrorException("unavailable"));
    try {
      urlRateLimiter.limit("appid", "http://www.eudemon.com/rt?key1=1&key2=2");
    } finally {
      verify(chain, times(1)).doBeforeLimit(any(), any());
      verify(chain, times(1)).doAfterLimit(any(), any(), any(), eq(false), isA(InternalErrorException.class));
    }
  }

  public void testGetRateLimiterAlgorithm() throws Exception {
    // rule
    RateLimitRule rule = Mockito.mock(RateLimitRule.class);
    when(rule.getLimit(any(), any())).thenReturn(new ApiLimit("/rt", 10));

    // chain
    RateLimiterInterceptorChain chain = new RateLimiterInterceptorChain();

    AbstractUrlRateLimiter urlRateLimiter = new AbstractUrlRateLimiter(rule, chain) {

      @Override
      protected RateLimiter createRateLimitAlgorithm(String limitKey, int limit) {
        return new RateLimiter() {

          @Override
          public boolean tryAcquire() throws InternalErrorException {
            return false;
          }
        };
      }
    };

    RateLimiter limiter1 = urlRateLimiter.getRateLimiterAlgorithm("apiId-1", "/api1", 10);
    RateLimiter limiter2 = urlRateLimiter.getRateLimiterAlgorithm("apiId-1", "/api1", 10);
    assertSame(limiter1, limiter2);
  }

  public void testGetRateLimiterAlgorithm_runOnMultiThreads() {
    // TODO
  }

}
