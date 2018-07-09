package com.eudemon.ratelimiter.rule.parser;

import com.eudemon.ratelimiter.exception.ConfigurationResolveException;
import com.eudemon.ratelimiter.rule.ApiLimit;
import com.eudemon.ratelimiter.rule.parser.JsonRuleConfigParser;
import com.eudemon.ratelimiter.rule.source.UniformRuleConfigMapping;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@Test
public class JsonRuleConfigParserTest {

  private static final String VALID_JSON_1 = "{ \"configs\": [\n" +
      "  {\n" +
      "    \"appId\": \"app-1\",\n" +
      "    \"limits\": [\n" +
      "     {\n" +
      "       \"api\": \"/v1/user\",\n" +
      "       \"limit\": \"100\",\n" +
      "       \"unit\": \"50\"\n" +
      "     },\n" +
      "     {\n" +
      "       \"api\": \"/v1/user/borrower\",\n" +
      "       \"limit\": \"50\",\n" +
      "       \"unit\": \"10\"\n" +
      "     }\n" +
      "    ]\n" +
      "  },\n" +
      "  {\n" +
      "    \"appId\": \"app-2\",\n" +
      "    \"limits\": [\n" +
      "     {\n" +
      "       \"api\": \"/v1/plan/{planId}/sell\",\n" +
      "       \"limit\": \"20\",\n" +
      "       \"unit\": \"20\"\n" +
      "     },\n" +
      "     {\n" +
      "       \"api\": \"/v1/loan/\",\n" +
      "       \"limit\": \"100\"\n" +
      "     }\n" +
      "    ]\n" +
      "  }\n" +
      "] }";

  private static final String INVALID_JSON_1 = "invalid json";

  public void testParse() {
    JsonRuleConfigParser parser = new JsonRuleConfigParser();
    UniformRuleConfigMapping result = parser.parse(VALID_JSON_1);
    Assert.assertNotNull(result);
    print(result);
  }

  @Test(expectedExceptions = {ConfigurationResolveException.class})
  public void testParse_withInvalidJson() {
    JsonRuleConfigParser parser = new JsonRuleConfigParser();
    parser.parse(INVALID_JSON_1);
  }

  public void testParse_withInputStream() {
    InputStream inputStream = new ByteArrayInputStream(VALID_JSON_1.getBytes());
    JsonRuleConfigParser parser = new JsonRuleConfigParser();
    UniformRuleConfigMapping result = parser.parse(inputStream);
    Assert.assertNotNull(result);
    System.out.println(result);
  }

  @Test(expectedExceptions = {ConfigurationResolveException.class})
  public void testParse_withInvalidInputStream() {
    InputStream inputStream = new ByteArrayInputStream(INVALID_JSON_1.getBytes());
    JsonRuleConfigParser parser = new JsonRuleConfigParser();
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
