package com.eudemon.ratelimiter.benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.eudemon.ratelimiter.DistributedUrlRateLimiter;
import com.eudemon.ratelimiter.MemoryUrlRateLimiter;
import com.eudemon.ratelimiter.UrlRateLimiter;
import com.eudemon.ratelimiter.Interceptor.RateLimiterInterceptorChain;
import com.eudemon.ratelimiter.exception.InternalErrorException;
import com.eudemon.ratelimiter.exception.InvalidUrlException;
import com.eudemon.ratelimiter.exception.OverloadException;
import com.eudemon.ratelimiter.redis.DefaultJedisPoolConfig;
import com.eudemon.ratelimiter.redis.DefaultJedisTaskExecutor;
import com.eudemon.ratelimiter.redis.JedisTaskExecutor;
import com.eudemon.ratelimiter.rule.ApiLimit;
import com.eudemon.ratelimiter.rule.UrlRateLimitRule;
import com.eudemon.ratelimiter.utils.UrlUtils;

public class RateLimiterBenchmarkTest {

  private static List<Case> cases = Arrays.asList(new Case[] {
      /* warm up */
      new Case(1, 10000, 100),

      /* concurrency */
      // new Case(1, 100000, 1),
      // new Case(3, 100000, 1),
      // new Case(6, 100000, 1),
      // new Case(12, 100000, 1),
      // new Case(24, 100000, 1),
      // new Case(48, 10000, 1),
      // new Case(100, 10000, 1),
      // new Case(200, 10000, 1),

      /* url */
      // new Case(12, 10000, 1),
      // new Case(12, 10000, 2),
      // new Case(12, 10000, 4),
      // new Case(12, 10000, 8),
      // new Case(12, 10000, 12),
      // new Case(12, 10000, 24),
      // new Case(12, 10000, 48),

      /* thinking time */
      // new Case(100, 10000, 100, 0),
      // new Case(100, 1000000, 100, 5),
      // new Case(100, 10000, 100, 2),
      // new Case(100, 10000, 100, 4),
      // new Case(100, 10000, 100, 6),
      // new Case(100, 10000, 100, 8),
      // new Case(100, 10000, 100, 10),
      // new Case(100, 1000, 100, 20),
  });

  private static String APP_ID = "api-1";
  private static String URL_BASE_PART = "http://www.test.com/user";

  public static void main(String[] args) {
    System.out.println("-------------Rate limit benchmark------------");
    benchmark(false);
  }

  private static void benchmark(boolean isDistributed) {
    System.out.println("9999,999,99,90,max,avg,tps");
    for (Case c : cases) {
      List<String> urls = generateUrls(c.getUrlCount());
      UrlRateLimitRule rule;
      try {
        rule = buildRule(urls);
      } catch (InvalidUrlException e1) {
        System.out.println("generate urls error.");
        return;
      }

      UrlRateLimiter ratelimiter = null;
      if (isDistributed) {
        GenericObjectPoolConfig poolConfig = new DefaultJedisPoolConfig();
        poolConfig.setMaxTotal(c.getThreadCount() + 10);
        poolConfig.setMaxIdle(c.getThreadCount() + 10);
        JedisTaskExecutor jedisTaskExecutor =
            new DefaultJedisTaskExecutor("127.0.0.1:6379", 100, poolConfig);
        ratelimiter = new DistributedUrlRateLimiter(jedisTaskExecutor, rule,
            new RateLimiterInterceptorChain());
      } else {
        new MemoryUrlRateLimiter(rule, new RateLimiterInterceptorChain());
      }

      int threadCount = c.getThreadCount();
      CyclicBarrier barrier = new CyclicBarrier(threadCount);
      CountDownLatch latch = new CountDownLatch(threadCount);

      List<TestRunnable> runnables = new ArrayList<>();
      for (int j = 0; j < threadCount; ++j) {
        TestRunnable runnable = new TestRunnable(barrier, latch, c.getInvokeCount(),
            c.getSleepInMillis(), urls, ratelimiter);
        runnables.add(runnable);
      }

      long wallStartTime = System.nanoTime();
      for (int k = 0; k < threadCount; ++k) {
        Thread t = new Thread(runnables.get(k));
        t.start();
      }

      try {
        latch.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      long wallCostTime = System.nanoTime() - wallStartTime;

      List<Long> responseTimes = new ArrayList<>();
      for (TestRunnable r : runnables) {
        responseTimes.addAll(r.getResponseTimes());
      }
      if (!responseTimes.isEmpty()) {
        print(c, responseTimes, wallCostTime);
      } else {
        System.out.println("no print.");
      }

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private static void print(Case c, List<Long> responseTimes, long wallCostTime) {
    // System.out.println("Case: thread count=" + c.getThreadCount() +
    // ";invoke count="
    // + c.getInvokeCount() + ";url count=" + c.getUrlCount() + ";sleep=" +
    // c.getSleepInMillis());

    Collections.sort(responseTimes);

    int size = responseTimes.size();
    int idxMax = size - 1;
    int idx9999 = (int) (size * 0.9999 - 1);
    int idx999 = (int) (size * 0.999 - 1);
    int idx99 = (int) (size * 0.99 - 1);
    int idx90 = (int) (size * 0.9 - 1);

    float timeMax = (float) responseTimes.get(idxMax) / 1000000f;
    float time9999 = (float) responseTimes.get(idx9999) / 1000000f;
    float time999 = (float) responseTimes.get(idx999) / 1000000f;
    float time99 = (float) responseTimes.get(idx99) / 1000000f;
    float time90 = (float) responseTimes.get(idx90) / 1000000f;

    float avgTime = 0;
    for (Long t : responseTimes) {
      avgTime += t;
    }
    avgTime /= size;
    avgTime /= 1000000f;
    float ftps = c.invokeCount * c.threadCount;
    ftps = ftps * 1000 / (wallCostTime / 1000000f);
    StringBuilder report = new StringBuilder();
    report.append(String.format("%.5f", time9999) + ",");
    report.append(String.format("%.5f", time999) + ",");
    report.append(String.format("%.5f", time99) + ",");
    report.append(String.format("%.5f", time90) + ",");
    report.append(String.format("%.5f", timeMax) + ",");
    report.append(String.format("%.5f", avgTime) + ",");
    report.append((int) ftps + "");
    report.append("wall time:" + wallCostTime / 1000000f + "; ");
    System.out.println(report.toString());
  }

  private static class TestRunnable implements Runnable {

    private CyclicBarrier barrier;
    private CountDownLatch latch;
    private int invokeCount;
    private int sleepInMillis;

    private List<String> urls;

    private UrlRateLimiter urlRateLimiter;

    private List<Long> responseTimes = new ArrayList<>();

    public List<Long> getResponseTimes() {
      return responseTimes;
    }

    public TestRunnable(CyclicBarrier barrier, CountDownLatch latch, int invokeCount,
        int sleepInMillis, List<String> urls, UrlRateLimiter urlRateLimiter) {
      this.barrier = barrier;
      this.latch = latch;
      this.invokeCount = invokeCount;
      this.sleepInMillis = sleepInMillis;
      this.urls = urls;
      this.urlRateLimiter = urlRateLimiter;
    }

    @Override
    public void run() {
      Random r = new Random(System.nanoTime());
      try {
        barrier.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (BrokenBarrierException e) {
        e.printStackTrace();
      }

      for (int k = 0; k < invokeCount; ++k) {
        String url = urls.get(r.nextInt(urls.size()));
        long startTime = System.nanoTime();
        try {
          startTime = System.nanoTime();
          urlRateLimiter.limit(APP_ID, url);
        } catch (InvalidUrlException | OverloadException | InternalErrorException e) {
          e.printStackTrace();
        } finally {
          responseTimes.add(System.nanoTime() - startTime);
        }

        if (sleepInMillis > 0) {
          try {
            Thread.sleep(sleepInMillis);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }

      latch.countDown();
    }
  }

  private static final class Case {
    private int threadCount;
    private int invokeCount;
    private int urlCount;
    private int sleepInMillis;

    public Case(int threadCount, int invokeCount, int urlCount, int sleepInMillis) {
      this.threadCount = threadCount;
      this.invokeCount = invokeCount;
      this.urlCount = urlCount;
      this.sleepInMillis = sleepInMillis;
    }

    public Case(int threadCount, int invokeCount, int urlCount) {
      this(threadCount, invokeCount, urlCount, 0);
    }

    public int getThreadCount() {
      return this.threadCount;
    }

    public int getInvokeCount() {
      return this.invokeCount;
    }

    public int getUrlCount() {
      return this.urlCount;
    }

    public int getSleepInMillis() {
      return this.sleepInMillis;
    }
  }

  private static UrlRateLimitRule buildRule(List<String> urls) throws InvalidUrlException {
    UrlRateLimitRule rule = new UrlRateLimitRule();
    for (String url : urls) {
      rule.addLimit(APP_ID, new ApiLimit(UrlUtils.getUrlPath(url), 10000000));
    }
    return rule;
  }

  private static List<String> generateUrls(int count) {
    List<String> urls = new ArrayList<>();
    for (int i = 0; i < count; ++i) {
      StringBuilder b = new StringBuilder();
      b.append(URL_BASE_PART);
      b.append("/");
      b.append(generateRandomString(4));
      urls.add(b.toString());
    }
    return urls;
  }

  private static String generateRandomString(int length) {
    Random r = new Random(System.nanoTime());
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < length; ++i) {
      b.append((char) ('a' + r.nextInt(26)));
    }
    return b.toString();
  }

}
