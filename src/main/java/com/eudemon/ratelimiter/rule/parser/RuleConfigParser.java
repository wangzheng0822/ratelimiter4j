package com.eudemon.ratelimiter.rule.parser;

import java.io.InputStream;

import com.eudemon.ratelimiter.rule.source.UniformRuleConfigMapping;

/**
 * Interface of parser used to parse different types of configurations, such as JSON, YAML and etc.
 */
public interface RuleConfigParser {

  UniformRuleConfigMapping parse(String configurationText);

  UniformRuleConfigMapping parse(InputStream in);

}
