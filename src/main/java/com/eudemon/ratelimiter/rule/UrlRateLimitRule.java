package com.eudemon.ratelimiter.rule;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.eudemon.ratelimiter.exception.ConfigurationResolveException;
import com.eudemon.ratelimiter.exception.InvalidUrlException;
import com.eudemon.ratelimiter.extension.Order;
import com.eudemon.ratelimiter.rule.source.UniformRuleConfigMapping;

/**
 * Rate limit rule which use trie tree to store limit rules. This class is thread-safe.
 */
@Order(Order.HIGHEST_PRECEDENCE + 10)
public class UrlRateLimitRule implements RateLimitRule {

  /**
   * store <appId, limit rules> pairs.
   */
  private volatile ConcurrentHashMap<String, AppUrlRateLimitRule> limitRules =
      new ConcurrentHashMap<>();

  public UrlRateLimitRule() {}

  @Override
  public void addRule(UniformRuleConfigMapping uniformRuleConfigMapping) {
    if (uniformRuleConfigMapping == null) {
      return;
    }
    List<UniformRuleConfigMapping.UniformRuleConfig> uniformRuleConfigs =
        uniformRuleConfigMapping.getConfigs();
    try {
      for (UniformRuleConfigMapping.UniformRuleConfig uniformRuleConfig : uniformRuleConfigs) {
        String appId = uniformRuleConfig.getAppId();
        addLimits(appId, uniformRuleConfig.getLimits());
      }
    } catch (InvalidUrlException e) {
      throw new ConfigurationResolveException("rule configuration is invalid: ", e);
    }
  }

  @Override
  public ApiLimit getLimit(String appId, String urlPath) throws InvalidUrlException {
    if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(urlPath)) {
      return null;
    }

    AppUrlRateLimitRule appUrlRateLimitRule = limitRules.get(appId);
    if (appUrlRateLimitRule == null) {
      return null;
    }

    ApiLimit apiLimit = appUrlRateLimitRule.getLimitInfo(urlPath);
    return apiLimit;
  }

  @Override
  public void addLimit(String appId, ApiLimit apiLimit) throws InvalidUrlException {
    if (StringUtils.isEmpty(appId) || apiLimit == null) {
      return;
    }

    AppUrlRateLimitRule newTrie = new AppUrlRateLimitRule();
    AppUrlRateLimitRule trie = limitRules.putIfAbsent(appId, newTrie);
    if (trie == null) {
      newTrie.addLimitInfo(apiLimit);
    } else {
      trie.addLimitInfo(apiLimit);
    }
  }

  @Override
  public void addLimits(String appId, List<ApiLimit> limits) throws InvalidUrlException {
    AppUrlRateLimitRule newTrie = new AppUrlRateLimitRule();
    AppUrlRateLimitRule trie = limitRules.putIfAbsent(appId, newTrie);
    if (trie == null) {
      trie = newTrie;
    }
    for (ApiLimit apiLimit : limits) {
      trie.addLimitInfo(apiLimit);
    }
  }

  @Override
  public void rebuildRule(UniformRuleConfigMapping uniformRuleConfigMapping) {
    ConcurrentHashMap<String, AppUrlRateLimitRule> newLimitRules = new ConcurrentHashMap<>();
    List<UniformRuleConfigMapping.UniformRuleConfig> uniformRuleConfigs =
        uniformRuleConfigMapping.getConfigs();
    for (UniformRuleConfigMapping.UniformRuleConfig uniformRuleConfig : uniformRuleConfigs) {
      String appId = uniformRuleConfig.getAppId();
      AppUrlRateLimitRule appUrlRateLimitRule = new AppUrlRateLimitRule();
      newLimitRules.put(appId, appUrlRateLimitRule);
      try {
        for (ApiLimit apiLimit : uniformRuleConfig.getLimits()) {
          appUrlRateLimitRule.addLimitInfo(apiLimit);
        }
      } catch (InvalidUrlException e) {
        throw new ConfigurationResolveException("rule configuration is invalid: ", e);
      }
    }
    limitRules = newLimitRules;
  }

}
