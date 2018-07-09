package com.eudemon.ratelimiter.algorithm;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.eudemon.ratelimiter.exception.InternalErrorException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;

/**
 *  This class implements fixed time window rate limiter algorithm.
 *  This class is thread-safe.
 */
public class FixedTimeWindowRateLimiter implements RateLimiter {
  
  /* timeout for {@code Lock.tryLock() }. */
  private static final long TRY_LOCK_TIMEOUT = 200L;  // 200ms.

  private Stopwatch stopwatch;

  private AtomicInteger currentCount = new AtomicInteger(0);

  /* the max permitted access count per second */
  private final int limit;

  private Lock lock = new ReentrantLock();

  public FixedTimeWindowRateLimiter(int limit) {
    this(limit, Stopwatch.createStarted());
  }

  @VisibleForTesting
  protected FixedTimeWindowRateLimiter(int limit, Stopwatch stopwatch) {
    this.limit = limit;
    this.stopwatch = stopwatch;
  }

  /**
   * try to acquire an access token.
   * 
   * @return true if get an access token successfully, otherwise, return false.
   * @throws InternalErrorException if some internal error occurs.
   */
  @Override
  public boolean tryAcquire() throws InternalErrorException {
    int updatedCount = currentCount.incrementAndGet();
    if (updatedCount <= limit) {
      return true;
    }

    try {
      if (lock.tryLock(TRY_LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
        try {
          if (stopwatch.elapsed(TimeUnit.MILLISECONDS) > TimeUnit.SECONDS.toMillis(1)) {
            currentCount.set(0);
            stopwatch.reset();
          }
          updatedCount = currentCount.incrementAndGet();
          return updatedCount <= limit;
        } finally {
          lock.unlock();
        }
      } else {
        throw new InternalErrorException("tryAcquire() wait lock too long:" + TRY_LOCK_TIMEOUT + "ms");
      }
    } catch (InterruptedException e) {
      throw new InternalErrorException("tryAcquire() is interrupted by lock-time-out.", e);
    }
  }
}
