package com.eudemon.ratelimiter.env.resolver;

import org.hamcrest.collection.IsMapContaining;
import org.testng.annotations.Test;

import com.eudemon.ratelimiter.env.resolver.PropertiesPropertySourceResolver;
import com.eudemon.ratelimiter.env.resolver.PropertySourceResolver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test
public class PropertiesPropertySourceResolverTest {

  public void testResolve() throws UnsupportedEncodingException {
    PropertySourceResolver resolver = new PropertiesPropertySourceResolver();
    String str = "ratelimiter.rule.source=zookeeper\n"
        + "ratelimiter.rule.parser=yaml\n"
        + "ratelimiter.redis.ip=12.34.22.123";
    InputStream inputStream = new ByteArrayInputStream(str.getBytes("UTF-8"));
    Map<String, Object> actualProperties = resolver.resolve(inputStream);
    assertThat(
        actualProperties, IsMapContaining.hasEntry("ratelimiter.rule.source", "zookeeper"));
    assertThat(
        actualProperties, IsMapContaining.hasEntry("ratelimiter.rule.parser", "yaml"));
    assertThat(
        actualProperties, IsMapContaining.hasEntry("ratelimiter.redis.ip", "12.34.22.123"));
    assertNull(actualProperties.get("not-existing-key"));
  }

  public void testResolve_withInvalidInput() throws UnsupportedEncodingException {
    String invalidString = "invalid-string";
    InputStream inputStream = new ByteArrayInputStream(invalidString.getBytes("UTF-8"));
    PropertySourceResolver resolver = new PropertiesPropertySourceResolver();
    Map<String, Object> properties = resolver.resolve(inputStream);
    assertNotNull(properties);
    assertTrue(properties.isEmpty());
  }

}
