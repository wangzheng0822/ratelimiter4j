package com.eudemon.ratelimiter.env;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class PropertySource {

  private final Map<String, Object> properties = new LinkedHashMap<String, Object>();

  public PropertySource() {}

  public PropertySource(Map<String, Object> properties) {
    this.properties.putAll(properties);
  }

  public void addProperties(Map<String, Object> properties) {
    this.properties.putAll(properties);
  }

  public void combinePropertySource(PropertySource propertySource) {
    if (propertySource == null || propertySource.getProperties().isEmpty()) {
      return;
    }
    addProperties(propertySource.getProperties());
  }

  public boolean containsKey(String name) {
    return properties.containsKey(name);
  }

  public Object getPropertyValue(String name) {
    return properties.get(name);
  }

  public Map<String, Object> getProperties() {
    return this.properties;
  }

  public String[] getPropertyNames() {
    Collection<String> names = this.properties.keySet();
    if (names == null) {
      return null;
    }
    return names.toArray(new String[names.size()]);
  }

  public String getPropertyStringValue(String name) {
    Object oval = getPropertyValue(name);
    if (oval == null) {
      return null;
    }

    if (oval instanceof String) {
      return (String) oval;
    }

    String sval = String.valueOf(oval);
    return sval;
  }

  public Integer getPropertyIntValue(String name) {
    Object oval = getPropertyValue(name);
    if (oval == null) {
      return null;
    }

    if (oval instanceof Integer) {
      return (Integer) oval;
    }

    try {
      return Integer.valueOf(getPropertyStringValue(name));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public Boolean getPropertyBooleanValue(String name) {
    Object oval = getPropertyValue(name);
    if (oval == null) {
      return null;
    }

    if (oval instanceof Boolean) {
      return (Boolean) oval;
    }
    
    if ("true".equalsIgnoreCase(getPropertyStringValue(name))) {
      return Boolean.TRUE;
    }
    
    if ("false".equalsIgnoreCase(getPropertyStringValue(name))) {
      return Boolean.FALSE;
    }
    
    return null;
  }

}
