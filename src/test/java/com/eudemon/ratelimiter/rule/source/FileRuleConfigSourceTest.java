package com.eudemon.ratelimiter.rule.source;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.eudemon.ratelimiter.rule.source.FileRuleConfigSource;
import com.eudemon.ratelimiter.rule.source.RuleConfigSource;
import com.eudemon.ratelimiter.rule.source.UniformRuleConfigMapping;

/**
 * TODO(zheng): add more tests.
 */
@Test
public class FileRuleConfigSourceTest {

  public void testLoad() {
    RuleConfigSource source = new FileRuleConfigSource();
    UniformRuleConfigMapping ruleConfigMapping = source.load();
    Assert.assertNotNull(ruleConfigMapping);
  }

}
