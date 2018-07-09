package com.eudemon.ratelimiter.rule.source;

import static com.eudemon.ratelimiter.context.RateLimiterBeansFactory.BEANS_CONTEXT;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eudemon.ratelimiter.env.RateLimiterConfig;
import com.eudemon.ratelimiter.extension.Order;
import com.eudemon.ratelimiter.rule.parser.RuleConfigParser;
import com.google.common.annotations.VisibleForTesting;

/**
 * This class is responsible to load rate limit rule configuration from Zookeeper.
 */
@Order(Order.HIGHEST_PRECEDENCE + 20)
public class ZookeeperRuleConfigSource implements RuleConfigSource {

  private static final Logger logger = LoggerFactory.getLogger(ZookeeperRuleConfigSource.class);

  private static final Charset UTF8 = Charset.forName("UTF-8");
  private static final int MAX_RETRIES = 3;
  private static final int BASE_SLEEP_TIME_MS = (int) TimeUnit.SECONDS.toMillis(1);
  private static final int TIMEOUT = (int) TimeUnit.SECONDS.toMillis(3);

  /* zookeeper server address. */
  private String address;

  /* node path of rate limit rule configuration. */
  private String path;

  /* keep listening if the value is set to be TRUE. */
  private boolean needKeepAlive = false;

  /* max number of times to retry. */
  private int maxRetries = MAX_RETRIES;

  /* initial amount of time to wait between retries. */
  private int baseSleepTimeMs = BASE_SLEEP_TIME_MS;

  /* client->server connection timeout. */
  private int connectionTimeout = TIMEOUT;

  private int sessionTimeout = TIMEOUT;

  private CuratorFramework client;

  private RuleConfigParser ruleConfigParser;

  /* init zookeeper client only once. */
  private AtomicBoolean isInitialized = new AtomicBoolean(false);

  public ZookeeperRuleConfigSource() {
    this(null);
  }

  public ZookeeperRuleConfigSource(RuleConfigParser ruleConfigParser) {
    this((String) null, (String) null, ruleConfigParser);
  }

  public ZookeeperRuleConfigSource(String address, String path, RuleConfigParser ruleConfigParser) {
    RateLimiterConfig.instance().load();

    if (StringUtils.isEmpty(address)) {
      address = RateLimiterConfig.instance().getZookeeperConfig().getAddress();
    }
    if (StringUtils.isEmpty(path)) {
      path = RateLimiterConfig.instance().getZookeeperConfig().getPath();
    }

    this.address = address;
    this.path = path;
    this.ruleConfigParser = BEANS_CONTEXT.obtainRuleConfigParser(ruleConfigParser);
  }

  @VisibleForTesting
  protected ZookeeperRuleConfigSource(CuratorFramework client, RuleConfigParser ruleConfigParser) {
    this.client = client;
    this.ruleConfigParser = ruleConfigParser;
  }

  public void setRuleConfigParser(RuleConfigParser parser) {
    this.ruleConfigParser = parser;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setNeedKeepAlive(boolean needKeepAlive) {
    this.needKeepAlive = needKeepAlive;
  }

  public void setMaxRetries(int maxRetries) {
    this.maxRetries = maxRetries;
  }

  public void setBaseSleepTimeMs(int baseSleepTimeMs) {
    this.baseSleepTimeMs = baseSleepTimeMs;
  }

  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  @Override
  public UniformRuleConfigMapping load() {
    initialClient(); // init client.

    UniformRuleConfigMapping uniformRuleConfigMapping = loadRateLimitRule();

    if (!needKeepAlive) {
      CloseableUtils.closeQuietly(client);
      isInitialized.compareAndSet(true, false);
    }

    return uniformRuleConfigMapping;
  }

  private void initialClient() {
    if (!isInitialized.compareAndSet(false, true)) {
      return;
    }

    if (client == null && StringUtils.isEmpty(address)) {
      throw new RuntimeException("zookeeper server address is not set.");
    }

    boolean connected = false;
    try {
      if (client == null) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries);
        client = CuratorFrameworkFactory.builder().connectString(address).retryPolicy(retryPolicy)
            .connectionTimeoutMs(connectionTimeout).sessionTimeoutMs(sessionTimeout).build();
      }
      client.start();
      connected = client.blockUntilConnected(connectionTimeout, TimeUnit.MILLISECONDS);
      if (!connected) {
        throw new RuntimeException("connect zookeeper failed.");
      }
    } catch (Exception e) {
      CloseableUtils.closeQuietly(client);
      isInitialized.compareAndSet(true, false);
      throw new RuntimeException("init zookeeper client error.", e);
    }
  }

  private UniformRuleConfigMapping loadRateLimitRule() {
    byte[] result = null;
    try {
      result = client.getData().forPath(this.path);
    } catch (Exception e) {
      throw new RuntimeException("load rule from zookeeper failed.", e);
    }
    if (result != null && result.length != 0) {
      String configurationText = new String(result, UTF8);
      UniformRuleConfigMapping uniformRuleConfigMapping = ruleConfigParser.parse(configurationText);
      return uniformRuleConfigMapping;
    } else {
      logger.warn("configuration of zookeeper node path: {} is empty.", path);
    }

    return null;
  }

}
