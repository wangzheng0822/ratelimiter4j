package com.eudemon.ratelimiter.env.io;

import org.testng.annotations.Test;

import com.eudemon.ratelimiter.env.io.DefaultResourceLoader;
import com.eudemon.ratelimiter.env.io.Resource;
import com.eudemon.ratelimiter.env.io.ResourceLoader;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test
public class DefaultResourceLoaderTest {

  public void testGetResource() {
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    String location1 = "file:src/test/resources/ratelimiter-env.yaml";
    Resource r1 = resourceLoader.getResource(location1);
    assertTrue(r1.exists());

    String location2 = "src/test/resources/ratelimiter-env.yaml";
    Resource r2 = resourceLoader.getResource(location2);
    assertTrue(r2.exists());

    String location3 = "classpath:ratelimiter-env.yaml";
    Resource r3 = resourceLoader.getResource(location3);
    assertTrue(r3.exists());

    String location4 = "no-existing";
    Resource r4 = resourceLoader.getResource(location4);
    assertFalse(r4.exists());
  }

}
