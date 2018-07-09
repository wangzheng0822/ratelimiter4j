package com.eudemon.ratelimiter.interceptor;

import com.eudemon.ratelimiter.Interceptor.RateLimiterInterceptor;
import com.eudemon.ratelimiter.Interceptor.RateLimiterInterceptorChain;
import com.eudemon.ratelimiter.extension.Order;
import com.eudemon.ratelimiter.rule.ApiLimit;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

@Test
public class RateLimiterInterceptorChainTest {

  private List<String> orders = new ArrayList<String>();

  @Order(Order.HIGHEST_PRECEDENCE + 10)
  public class InterceptorA implements RateLimiterInterceptor {

    @Override
    public void beforeLimit(String appId, String api) {
      orders.add(identity() + ":before");
    }

    @Override
    public void afterLimit(String appId, String api, ApiLimit apiLimit, boolean result,
        Exception ex) {
      orders.add(identity() + ":after");
    }

    protected String identity() {
      return InterceptorA.class.getSimpleName();
    }
  }

  @Order(Order.HIGHEST_PRECEDENCE + 20)
  public class InterceptorB extends InterceptorA {
    @Override
    protected String identity() {
      return InterceptorB.class.getSimpleName();
    }
  }

  @Order(Order.HIGHEST_PRECEDENCE + 30)
  public class InterceptorC extends InterceptorA {
    @Override
    protected String identity() {
      return InterceptorC.class.getSimpleName();
    }
  }

  @BeforeMethod
  public void setup4eachCase() {
    orders.clear();
  }

  public void testDoBeforeLimit() {
    RateLimiterInterceptorChain chain = new RateLimiterInterceptorChain();
    chain.addInterceptor(new InterceptorB());
    chain.addInterceptor(new InterceptorA());
    chain.addInterceptor(new InterceptorC());
    chain.doBeforeLimit("app1", "api1");

    Assert.assertEquals(orders.size(), 3);
    Assert.assertEquals(orders.get(0), "InterceptorA:before");
    Assert.assertEquals(orders.get(1), "InterceptorB:before");
    Assert.assertEquals(orders.get(2), "InterceptorC:before");
  }

  public void testDoAfterLimit() {
    RateLimiterInterceptorChain chain = new RateLimiterInterceptorChain();
    chain.addInterceptor(new InterceptorB());
    chain.addInterceptor(new InterceptorA());
    chain.addInterceptor(new InterceptorC());
    chain.doAfterLimit("app1", "api1", null, true, null);

    Assert.assertEquals(orders.size(), 3);
    Assert.assertEquals(orders.get(0), "InterceptorC:after");
    Assert.assertEquals(orders.get(1), "InterceptorB:after");
    Assert.assertEquals(orders.get(2), "InterceptorA:after");
  }

  public void testIsEmpty() {
    RateLimiterInterceptorChain chain = new RateLimiterInterceptorChain();
    boolean isEmpty = chain.isEmpty();
    Assert.assertTrue(isEmpty);
  }

  public void testClear() {
    RateLimiterInterceptorChain chain = new RateLimiterInterceptorChain();
    chain.addInterceptor(new InterceptorB());
    chain.addInterceptor(new InterceptorA());
    chain.addInterceptor(new InterceptorC());
    Assert.assertEquals(chain.getInterceptors().size(), 3);
    chain.clear();
    Assert.assertEquals(chain.getInterceptors().size(), 0);
  }

}
