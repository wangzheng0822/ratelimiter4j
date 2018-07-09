package com.eudemon.ratelimiter.env.resolver;

import org.hamcrest.collection.IsMapContaining;
import org.testng.annotations.Test;

import com.eudemon.ratelimiter.env.resolver.PropertySourceResolver;
import com.eudemon.ratelimiter.env.resolver.YamlPropertySourceResolver;
import com.eudemon.ratelimiter.exception.ConfigurationResolveException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test
public class YamlPropertySourceResolverTest {

  public void testResolve() throws UnsupportedEncodingException {
    PropertySourceResolver resolver = new YamlPropertySourceResolver();
    String str = "ratelimiter.rule.source: zookeeper\n"
        + "ratelimiter.rule.parser: yaml\n"
        + "ratelimiter.redis.ip: 12.34.22.123";
    InputStream inputStream = new ByteArrayInputStream(str.getBytes("UTF-8"));
    Map<String, Object> actualProperties = resolver.resolve(inputStream);
    assertThat(actualProperties, IsMapContaining.hasEntry("ratelimiter.rule.source", "zookeeper"));
    assertThat(actualProperties, IsMapContaining.hasEntry("ratelimiter.rule.parser", "yaml"));
    assertThat(actualProperties, IsMapContaining.hasEntry("ratelimiter.redis.ip", "12.34.22.123"));
  }

  public void testResolve_withHierarchy() throws UnsupportedEncodingException {
    PropertySourceResolver resolver = new YamlPropertySourceResolver();
    String str = "ratelimiter:\n"
        + " rule:\n"
        + "  source: zookeeper\n"
        + "  parser: yaml\n"
        + "ratelimiter.redis.ip: 12.34.22.123";
    InputStream inputStream = new ByteArrayInputStream(str.getBytes("UTF-8"));
    Map<String, Object> actualProperties = resolver.resolve(inputStream);
    assertThat(actualProperties, IsMapContaining.hasEntry("ratelimiter.rule.source", "zookeeper"));
    assertThat(actualProperties, IsMapContaining.hasEntry("ratelimiter.rule.parser", "yaml"));
    assertThat(actualProperties, IsMapContaining.hasEntry("ratelimiter.redis.ip", "12.34.22.123"));
  }

  @Test(expectedExceptions = { ConfigurationResolveException.class })
  public void testResolve_withInvalidInput() throws UnsupportedEncodingException {
    String invalidString = "invalid-string";
    InputStream inputStream = new ByteArrayInputStream(invalidString.getBytes("UTF-8"));
    PropertySourceResolver resolver = new YamlPropertySourceResolver();
    Map<String, Object> properties = resolver.resolve(inputStream);
    assertNotNull(properties);
    assertTrue(properties.isEmpty());

   invalidString = "ratelimiter:\n"
       + " rule:\n"
       + "  source: zookeeper\n"
       + "  parser: yaml\n"
       + "ratelimiter.redis.ip";
    inputStream = new ByteArrayInputStream(invalidString.getBytes("UTF-8"));
    properties = resolver.resolve(inputStream);
  }

}
