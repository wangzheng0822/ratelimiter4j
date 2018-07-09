package com.eudemon.ratelimiter.rule.source;

import java.util.List;

import com.eudemon.ratelimiter.rule.ApiLimit;

public class UniformRuleConfigMapping {

  private List<UniformRuleConfig> configs;

  public List<UniformRuleConfig> getConfigs() {
    return configs;
  }

  public void setConfigs(List<UniformRuleConfig> configs) {
    this.configs = configs;
  }

  public static class UniformRuleConfig {

    private String appId;

    private List<ApiLimit> limits;

    public UniformRuleConfig() {}

    public UniformRuleConfig(String appId, List<ApiLimit> limits) {
      this.appId = appId;
      this.limits = limits;
    }

    public String getAppId() {
      return appId;
    }

    public void setAppId(String appId) {
      this.appId = appId;
    }

    public List<ApiLimit> getLimits() {
      return limits;
    }

    public void setLimits(List<ApiLimit> limits) {
      this.limits = limits;
    }

  }

}
