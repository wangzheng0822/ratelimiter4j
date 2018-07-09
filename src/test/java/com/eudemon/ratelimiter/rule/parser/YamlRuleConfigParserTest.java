package com.eudemon.ratelimiter.rule.parser;

import com.eudemon.ratelimiter.exception.ConfigurationResolveException;
import com.eudemon.ratelimiter.rule.ApiLimit;
import com.eudemon.ratelimiter.rule.parser.YamlRuleConfigParser;
import com.eudemon.ratelimiter.rule.source.UniformRuleConfigMapping;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Test
public class YamlRuleConfigParserTest {
  private static final String VALID_YAML_1 = "configs:\n" +
      "- appId: app-1\n" +
      "  limits:\n" +
      "  - unit: 50\n" +
      "    limit: 100\n" +
      "    api: v1/user\n" +
      "  - unit: 20\n" +
      "    limit: 50\n" +
      "    api: v1/order\n" +
      "- appId: app-2\n" +
      "  limits:\n" +
      "  - limit: 50\n" +
      "    api: v1/user\n" +
      "  - unit: 30\n" +
      "    limit: 50\n" +
      "    api: v1/order\n";

  private static final String INVALID_YAML_1 = "invalid json";

  public void testParse() throws IOException {
    YamlRuleConfigParser parser = new YamlRuleConfigParser();
    UniformRuleConfigMapping result = parser.parse(VALID_YAML_1);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.getConfigs().size() == 2);
    print(result);
    System.out.println(System.getProperty("line.separator"));
  }

  @Test(expectedExceptions = {ConfigurationResolveException.class } )
  public void testParse_withInvalidYaml() throws YAMLException {
    YamlRuleConfigParser parser = new YamlRuleConfigParser();
    parser.parse(INVALID_YAML_1);
  }

  public void testParse_withInputStream() {
    InputStream inputStream = new ByteArrayInputStream(VALID_YAML_1.getBytes());
    YamlRuleConfigParser parser = new YamlRuleConfigParser();
    UniformRuleConfigMapping result = parser.parse(inputStream);
    Assert.assertNotNull(result);
    System.out.println(result);
  }

  @Test(expectedExceptions = {ConfigurationResolveException.class } )
  public void testParse_withInvalidInputStream() throws YAMLException {
    InputStream inputStream = new ByteArrayInputStream(INVALID_YAML_1.getBytes());
    YamlRuleConfigParser parser = new YamlRuleConfigParser();
    parser.parse(inputStream);
  }

  private void print(UniformRuleConfigMapping uniformRuleConfigMapping) {
    List<UniformRuleConfigMapping.UniformRuleConfig> list = uniformRuleConfigMapping.getConfigs();
    for (UniformRuleConfigMapping.UniformRuleConfig config : list) {
      System.out.println(config.getAppId());
      for (ApiLimit limit : config.getLimits()) {
        System.out.println(limit);
      }
      System.out.println();
    }
  }

}
