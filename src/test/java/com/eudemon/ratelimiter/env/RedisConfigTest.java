package com.eudemon.ratelimiter.env;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import com.eudemon.ratelimiter.env.PropertyConstants;
import com.eudemon.ratelimiter.env.PropertySource;
import com.eudemon.ratelimiter.env.RedisConfig;

@Test
public class RedisConfigTest {

  public void testBuildFromProperties() {
    PropertySource propertySource = new PropertySource();
    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put(PropertyConstants.PROPERTY_REDIS_ADDRESS, "1.1.1.1:111");
    properties.put(PropertyConstants.PROPERTY_REDIS_TIMEOUT, 123);
    properties.put(PropertyConstants.PROPERTY_REDIS_MAX_TOTAL, 101);
    properties.put(PropertyConstants.PROPERTY_REDIS_MAX_IDLE, 100);
    properties.put(PropertyConstants.PROPERTY_REDIS_MIN_IDLE, 33);
    properties.put(PropertyConstants.PROPERTY_REDIS_TEST_ON_BORROW, true);
    propertySource.addProperties(properties);

    RedisConfig redisConfig = new RedisConfig();
    redisConfig.buildFromProperties(propertySource);
    assertEquals(redisConfig.getAddress(), "1.1.1.1:111");
    assertEquals(redisConfig.getTimeout(), 123);
    assertNotNull(redisConfig.getPoolConfig());
    assertEquals(redisConfig.getPoolConfig().getMaxTotal(), 101);
    assertEquals(redisConfig.getPoolConfig().getMaxIdle(), 100);
    assertEquals(redisConfig.getPoolConfig().getMinIdle(), 33);
    assertEquals(redisConfig.getPoolConfig().getTestOnBorrow(), true);
  }

  public void testBuildFromProperties_withPartlyEmptyProperties() {
    RedisConfig redisConfig = new RedisConfig();
    PropertySource propertySource = new PropertySource();
    redisConfig.buildFromProperties(propertySource);
    assertTrue(StringUtils.isEmpty(redisConfig.getAddress()));
    assertEquals(redisConfig.getTimeout(), RedisConfig.DEFAULT_TIMEOUT);
    assertNotNull(redisConfig.getPoolConfig());
  }

  public void testBuildFromProperties_withInvalidTypeProperties() {
    PropertySource propertySource = new PropertySource();
    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put(PropertyConstants.PROPERTY_REDIS_ADDRESS, "1.1.1.1:abc");
    properties.put(PropertyConstants.PROPERTY_REDIS_TIMEOUT, "def");
    propertySource.addProperties(properties);

    RedisConfig redisConfig = new RedisConfig();
    redisConfig.buildFromProperties(propertySource);
    assertEquals(redisConfig.getAddress(), "1.1.1.1:abc");
    assertEquals(redisConfig.getTimeout(), RedisConfig.DEFAULT_TIMEOUT);
    assertNotNull(redisConfig.getPoolConfig());
  }

}
