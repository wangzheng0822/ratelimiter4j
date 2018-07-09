package com.eudemon.ratelimiter.integration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.eudemon.ratelimiter.DistributedUrlRateLimiter;
import com.eudemon.ratelimiter.MemoryUrlRateLimiter;
import com.eudemon.ratelimiter.UrlRateLimiter;
import com.eudemon.ratelimiter.env.PropertyConstants;
import com.eudemon.ratelimiter.env.RedisConfig;
import com.eudemon.ratelimiter.env.ZookeeperConfig;
import com.eudemon.ratelimiter.exception.InternalErrorException;
import com.eudemon.ratelimiter.exception.InvalidUrlException;
import com.eudemon.ratelimiter.exception.OverloadException;

/**
 * exceptions:
 * 
 * case 1: zookeeper access failed/timeout.
 * 
 * case 2: zookeeper does not have the node path.
 * 
 * case 3: config file not existed.
 * 
 * case 4: config invalid.
 * 
 * case 5: redis connection failed.
 * 
 * case 6: redis access timeout.
 */
@Test
public class UrlRateLimiterIT {

  private static final String REDIS_IP_PORT = "127.0.0.1:6379";
  private static final int REDIS_TIMEOUT = 100;

  private static final String ZK_ADDR = "127.0.0.1:2181";
  private static final String ZK_PATH = "/com/eudemon/ratelimit";

  private static final ZookeeperConfig zookeeperConfig = new ZookeeperConfig();
  private static final RedisConfig redisConfig = new RedisConfig();

  @BeforeClass
  public void setup() {
    System.setProperty(PropertyConstants.PROPERTY_REDIS_ADDRESS, REDIS_IP_PORT);
    System.setProperty(PropertyConstants.PROPERTY_REDIS_TIMEOUT, String.valueOf(REDIS_TIMEOUT));
    System.setProperty(PropertyConstants.PROPERTY_ZOOKEEPER_ADDRESS, ZK_ADDR);
    System.setProperty(PropertyConstants.PROPERTY_ZOOKEEPER_RULE_PATH, ZK_PATH);

    redisConfig.setAddress(REDIS_IP_PORT);
    redisConfig.setTimeout(REDIS_TIMEOUT);
    zookeeperConfig.setAddress(ZK_ADDR);
    zookeeperConfig.setPath(ZK_PATH);
  }

  @DataProvider(name = "params")
  public Object[][] createCaseParams() {
    return new Object[][] {
        // {"memory", "file", "yaml"},
        // { "memory", "file", "json" },
        // {"memory", "zookeeper", "yaml"},
        // { "memory", "zookeeper", "json" },
        // {"memory", "default", "default"},
        // { "memory", "zookeeper", "spi" },
        {"distributed", "file", "yaml"},
        // { "distributed", "file", "json" },
        // { "distributed", "zookeeper", "yaml" },
        // { "distributed", "zookeeper", "json" },
        // {"distributed", "default", "default"},
        // { "distributed", "zookeeper", "spi" }
    };
  }

  /**
   * Run limit() on single-threads for different configurations.
   */
  @Test(enabled = false, dataProvider = "params")
  public void test_runOnSingleThread(String type, String source, String parser) {
    System.out.println("case: [" + type + ":" + source + ":" + parser + "]");
    UrlRateLimiter ratelimiter = null;
    if (type.equals("distributed")) {
      if (parser.equals("yaml") || parser.equals("json")) {
        DistributedUrlRateLimiter.builder.setRuleParserType(parser);
      }
      if (source.equals("zookeeper") || source.equals("file")) {
        DistributedUrlRateLimiter.builder.setRuleSourceType(source);
      }
      if (source.equals("zookeeper")) {
        DistributedUrlRateLimiter.builder.setZookeeperConfig(zookeeperConfig);
      }
      ratelimiter = DistributedUrlRateLimiter.builder.setRedisConfig(redisConfig).build();
    } else {
      if (parser.equals("yaml") || parser.equals("json")) {
        MemoryUrlRateLimiter.builder.setRuleParserType(parser);
      }
      if (source.equals("zookeeper") || source.equals("file")) {
        MemoryUrlRateLimiter.builder.setRuleSourceType(source);
      }
      if (source.equals("zookeeper")) {
        MemoryUrlRateLimiter.builder.setZookeeperConfig(zookeeperConfig);
      }
      try {
        ratelimiter = MemoryUrlRateLimiter.builder.build();
      } catch (Exception e) {
        System.out.println("exception here.");
      }
    }

    runlimit(ratelimiter);
  }

  @DataProvider(name = "distributedOrMemory")
  public Object[][] distributedOrMemory() {
    return new Object[][] {{"memory"}, {"distributed"}};
  }

  /**
   * Run limit() on multi-threads. rule source: default(file); rule format: default(yaml)
   */
  @Test(enabled = false, dataProvider = "distributedOrMemory")
  public void test_runOnMultiThreads(String type) throws Exception {
    System.out.println("case: [" + type + "]");
    final UrlRateLimiter ratelimiter;
    if (type.equals("distributed")) {
      ratelimiter = new DistributedUrlRateLimiter();
    } else {
      ratelimiter = new MemoryUrlRateLimiter();
    }

    int MAX_THREAD_SIZE = 8;
    ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD_SIZE);
    CountDownLatch latch = new CountDownLatch(MAX_THREAD_SIZE);
    for (int i = 0; i < MAX_THREAD_SIZE; i++) {
      pool.submit(() -> {
        runlimit(ratelimiter);
        latch.countDown();
      });
    }

    latch.await();
    pool.shutdown();
  }

  private void runlimit(UrlRateLimiter ratelimiter) {
    for (int i = 0; i < 20; ++i) {
      try {
        ratelimiter.limit("app1", "http://www.test.com/user/24607172");
        System.out.println("limit PASS.");
      } catch (OverloadException | InvalidUrlException | InternalErrorException e) {
        System.out.println("limit FAILED: " + e.getClass().getSimpleName());
      }
    }

    System.out.println("SLEEP 1 SECOND.....");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    for (int i = 0; i < 20; ++i) {
      try {
        ratelimiter.limit("app1", "http://www.test.com/user/24607172");
        System.out.println("limit PASS.");
      } catch (OverloadException | InvalidUrlException | InternalErrorException e) {
        System.out.println("limit FAILED: " + e.getClass().getSimpleName());
      }
    }
  }

}
