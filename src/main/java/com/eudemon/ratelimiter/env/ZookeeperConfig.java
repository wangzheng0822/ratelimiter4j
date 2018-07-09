package com.eudemon.ratelimiter.env;

import org.apache.commons.lang3.StringUtils;

/**
 * Zookeeper configuration.
 */
public class ZookeeperConfig {

  public static final String DEFAULT_PATH = "/com/eudemon/ratelimit";

  private String address;

  private String path = DEFAULT_PATH;

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void buildFromProperties(PropertySource propertySource) {
    if (propertySource == null) {
      return;
    }

    String addr = propertySource.getPropertyStringValue(PropertyConstants
        .PROPERTY_ZOOKEEPER_ADDRESS);
    if (StringUtils.isNotBlank(addr)) {
      this.address = addr;
    }

    String path = propertySource.getPropertyStringValue(PropertyConstants
        .PROPERTY_ZOOKEEPER_RULE_PATH);
    if (StringUtils.isNotBlank(path)) {
      this.path = path;
    }
  }

}
