package com.eudemon.ratelimiter.rule.parser;

import com.eudemon.ratelimiter.extension.Order;
import com.eudemon.ratelimiter.rule.source.UniformRuleConfigMapping;
import com.eudemon.ratelimiter.utils.YamlUtils;

import java.io.InputStream;

/**
 * Parser used to parse YAML formatted configuration into {@link UniformRuleConfigMapping}
 */
@Order(Order.HIGHEST_PRECEDENCE + 10)
public class YamlRuleConfigParser implements RuleConfigParser {

  @Override
  public UniformRuleConfigMapping parse(String configurationText) {
    return YamlUtils.parse(configurationText, UniformRuleConfigMapping.class);
  }

  @Override
  public UniformRuleConfigMapping parse(InputStream in) {
    return YamlUtils.parse(in, UniformRuleConfigMapping.class);
  }

}
