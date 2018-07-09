package com.eudemon.ratelimiter.algorithm;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.eudemon.ratelimiter.algorithm.DistributedFixedTimeWindowRateLimiter;
import com.eudemon.ratelimiter.algorithm.RateLimiter;
import com.eudemon.ratelimiter.exception.InternalErrorException;
import com.eudemon.ratelimiter.redis.JedisTaskExecutor;

import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisNoScriptException;

@Test
public class DistributedFixedTimeWindowRateLimiterTest {
  
  public void testTryAquire_evalsha_ok() throws InternalErrorException {
    JedisTaskExecutor executor = Mockito.mock(JedisTaskExecutor.class);
    RateLimiter ratelimiter = new DistributedFixedTimeWindowRateLimiter("test-key", 5, executor);
    
    when(executor.evalsha(any(), any(), any())).thenReturn(1l);
    boolean passed = ratelimiter.tryAcquire();
    assertTrue(passed);
    
    when(executor.evalsha(any(), any(), any())).thenReturn(0l);
    passed = ratelimiter.tryAcquire();
    assertFalse(passed);
  }
  
  public void testTryAquire_withJedisNoScriptException() throws InternalErrorException {
    JedisTaskExecutor executor = Mockito.mock(JedisTaskExecutor.class);
    RateLimiter ratelimiter = new DistributedFixedTimeWindowRateLimiter("test-key", 5, executor);
    
    when(executor.evalsha(any(), any(), any())).thenThrow(new JedisNoScriptException(""));
    when(executor.eval(any(), any(), any())).thenReturn(1l);
    boolean passed = ratelimiter.tryAcquire();
    assertTrue(passed);
    
    when(executor.eval(any(), any(), any())).thenReturn(0l);
    passed = ratelimiter.tryAcquire();
    assertFalse(passed);
  }
  
  @Test(expectedExceptions = { InternalErrorException.class })
  public void testTryAquire_evalsha_JedisConnectionException() throws InternalErrorException {
    JedisTaskExecutor executor = Mockito.mock(JedisTaskExecutor.class);
    RateLimiter ratelimiter = new DistributedFixedTimeWindowRateLimiter("test-key", 5, executor);
    
    when(executor.evalsha(any(), any(), any())).thenThrow(new JedisConnectionException(""));
    ratelimiter.tryAcquire();
  }
  
  @Test(expectedExceptions = { InternalErrorException.class })
  public void testTryAquire_eval_JedisConnectionException() throws InternalErrorException {
    JedisTaskExecutor executor = Mockito.mock(JedisTaskExecutor.class);
    RateLimiter ratelimiter = new DistributedFixedTimeWindowRateLimiter("test-key", 5, executor);
    
    when(executor.evalsha(any(), any(), any())).thenThrow(new JedisNoScriptException(""));
    when(executor.eval(any(), any(), any())).thenThrow(new JedisConnectionException(""));
    ratelimiter.tryAcquire();
  }
  
}
