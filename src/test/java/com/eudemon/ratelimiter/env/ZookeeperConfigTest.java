package com.eudemon.ratelimiter.env;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import com.eudemon.ratelimiter.env.PropertyConstants;
import com.eudemon.ratelimiter.env.PropertySource;
import com.eudemon.ratelimiter.env.ZookeeperConfig;

@Test
public class ZookeeperConfigTest {

  public void testBuildFromProperties() {
    PropertySource propertySource = new PropertySource();
    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put(PropertyConstants.PROPERTY_ZOOKEEPER_ADDRESS, "1.1.1.1:1212");
    properties.put(PropertyConstants.PROPERTY_ZOOKEEPER_RULE_PATH, "/com/eudemon/ratelimit");
    propertySource.addProperties(properties);

    ZookeeperConfig zkConfig = new ZookeeperConfig();
    zkConfig.buildFromProperties(propertySource);
    assertEquals(zkConfig.getAddress(), "1.1.1.1:1212");
    assertEquals(zkConfig.getPath(), "/com/eudemon/ratelimit");
  }

  public void testBuildFromProperties_withEmptyProperties() {
    PropertySource propertySource = new PropertySource();
    ZookeeperConfig zkConfig = new ZookeeperConfig();
    zkConfig.buildFromProperties(propertySource);
    assertTrue(StringUtils.isEmpty(zkConfig.getAddress()));
    assertEquals(zkConfig.getPath(), ZookeeperConfig.DEFAULT_PATH);
  }

}
