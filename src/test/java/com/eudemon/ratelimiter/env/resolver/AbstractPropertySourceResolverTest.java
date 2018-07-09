package com.eudemon.ratelimiter.env.resolver;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.eudemon.ratelimiter.env.resolver.AbstractPropertySourceResolver;
import com.eudemon.ratelimiter.env.resolver.PropertySourceResolver;

import java.io.InputStream;
import java.util.Map;

@Test
public class AbstractPropertySourceResolverTest {

  public void testCanResolvedExtension() {
    PropertySourceResolver resolver = new AbstractPropertySourceResolver() {
      @Override
      public String[] getSupportedFileExtensions() {
        return new String[] { "abc", "properties" };
      }

      @Override
      public Map<String, Object> resolve(InputStream in) {
        return null;
      }
    };

    boolean actualRet = resolver.canResolvedExtension("abc");
    Assert.assertTrue(actualRet);

    actualRet = resolver.canResolvedExtension("properties");
    Assert.assertTrue(actualRet);

    actualRet = resolver.canResolvedExtension("def");
    Assert.assertFalse(actualRet);
  }

}
