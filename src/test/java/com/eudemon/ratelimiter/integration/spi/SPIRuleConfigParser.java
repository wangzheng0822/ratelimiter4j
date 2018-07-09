package com.eudemon.ratelimiter.integration.spi;

import java.io.InputStream;

import com.eudemon.ratelimiter.extension.Order;
import com.eudemon.ratelimiter.rule.parser.RuleConfigParser;
import com.eudemon.ratelimiter.rule.source.UniformRuleConfigMapping;

@Order(Order.HIGHEST_PRECEDENCE)
public class SPIRuleConfigParser implements RuleConfigParser {

  @Override
  public UniformRuleConfigMapping parse(String configurationText) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UniformRuleConfigMapping parse(InputStream in) {
    // TODO Auto-generated method stub
    return null;
  }

}
