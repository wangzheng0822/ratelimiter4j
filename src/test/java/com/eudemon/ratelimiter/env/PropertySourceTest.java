package com.eudemon.ratelimiter.env;

import org.hamcrest.collection.IsMapContaining;
import org.testng.annotations.Test;

import com.eudemon.ratelimiter.env.PropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Test
public class PropertySourceTest {

  public void testCombinePropertySource() {
    PropertySource propertySource = new PropertySource();
    Map<String, Object> m1 = new HashMap<>();
    m1.put("k1", "v1");
    m1.put("k2", "v2");
    propertySource.addProperties(m1);

    PropertySource propertySource2 = new PropertySource();
    Map<String, Object> m2 = new HashMap<>();
    m2.put("k1", "v1-1");
    m2.put("k3", "v3");
    propertySource2.addProperties(m2);

    propertySource.combinePropertySource(propertySource2);
    Map<String, Object> result = propertySource.getProperties();
    assertEquals(result.size(), 3);

    assertThat(result, IsMapContaining.hasEntry("k1", "v1-1"));
    assertThat(result, IsMapContaining.hasEntry("k2", "v2"));
    assertThat(result, IsMapContaining.hasEntry("k3", "v3"));
  }

  public void testGetPropertyValue() {
    PropertySource propertySource = new PropertySource();
    Map<String, Object> m1 = new HashMap<>();
    m1.put("k1", "v1");
    m1.put("k2", "v2");
    propertySource.addProperties(m1);

    assertEquals(propertySource.getPropertyValue("k1"), "v1");
    assertNull(propertySource.getPropertyValue("k3"));
  }

  public void testGetPropertyStringValue() {
    PropertySource propertySource = new PropertySource();
    Map<String, Object> m1 = new HashMap<>();
    m1.put("k1", "v1");
    m1.put("k2", 123);
    propertySource.addProperties(m1);

    assertEquals(propertySource.getPropertyStringValue("k1"), "v1");
    assertEquals(propertySource.getPropertyStringValue("k2"), "123");
  }

  public void testGetPropertyIntValue() {
    PropertySource propertySource = new PropertySource();
    Map<String, Object> m1 = new HashMap<>();
    m1.put("k1", "v1");
    m1.put("k2", 123);
    propertySource.addProperties(m1);

    assertNull(propertySource.getPropertyIntValue("k1"));
    assertEquals(propertySource.getPropertyIntValue("k2"), new Integer(123));
  }

  public void testGetPropertyBooleanValue() {
    PropertySource propertySource = new PropertySource();
    Map<String, Object> m1 = new HashMap<>();
    m1.put("k1", "v1");
    m1.put("k2", true);
    propertySource.addProperties(m1);
    assertNull(propertySource.getPropertyBooleanValue("k1"));
    assertEquals(propertySource.getPropertyBooleanValue("k2"), Boolean.TRUE);
  }

}
