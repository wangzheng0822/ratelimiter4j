package com.eudemon.ratelimiter.monitor;

import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eudemon.ratelimiter.UrlRateLimiter;
import com.eudemon.ratelimiter.env.RateLimiterConfig;
import com.eudemon.ratelimiter.rule.ApiLimit;

/**
 * The class is a monitor to collect {@link UrlRateLimiter#limit(String, String)} method key
 * performance indicator and give statistics report. This class is thread-safe. TODO(zheng): add
 * "TIMEOUT" statistics.
 */
public class MonitorManager {

  public static final int STATISTIC_PERIOD = 60; // 60s

  private static final Logger log = LoggerFactory.getLogger(MonitorManager.class);

  private static final MetricsCounter metricsCounter = new MetricsCounter();

  private static final ScheduledExecutorService scheduledExecutor =
      Executors.newScheduledThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
          return new Thread(r, "ratelimiter-monitor-thread");
        }
      });

  static {
    RateLimiterConfig.instance().load();

    scheduledExecutor.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          writeLog();
        } catch (Throwable e) {
          log.error("write log error:", e);
        }
      }
    }, STATISTIC_PERIOD, STATISTIC_PERIOD, TimeUnit.SECONDS);

    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        if (!scheduledExecutor.isShutdown()) {
          scheduledExecutor.shutdown();
        }
      }
    }));
  }

  public static void collect(String appId, String url, ApiLimit apiLimit, long duration,
      boolean result, Exception ex) {
    metricsCounter.increment(MetricType.TOTAL);

    if (result) {
      metricsCounter.increment(MetricType.PASSED);
    } else if (ex == null) {
      metricsCounter.increment(MetricType.LIMITED);
    }

    if (ex != null) {
      metricsCounter.increment(MetricType.EXCEPTION);
    }

    metricsCounter.add(MetricType.DURATION, duration);
  }

  public static void writeLog() {
    DecimalFormat mbFormat = new DecimalFormat("0.00");
    long total = metricsCounter.sumAndReset(MetricType.TOTAL);
    if (total == 0) {
      return;
    }
    long passed = metricsCounter.sumAndReset(MetricType.PASSED);
    long limited = metricsCounter.sumAndReset(MetricType.LIMITED);
    long exception = metricsCounter.sumAndReset(MetricType.EXCEPTION);
    float duration = metricsCounter.sumAndReset(MetricType.DURATION) / 1000f; // ms
    float avgDuration = duration / total;
    log.info(
        "[ratelimiter statistics] total:{}, passed:{}, limited:{}, exception:{}, avg duration:{}",
        total, passed, limited, exception, mbFormat.format(avgDuration));
  }

}
