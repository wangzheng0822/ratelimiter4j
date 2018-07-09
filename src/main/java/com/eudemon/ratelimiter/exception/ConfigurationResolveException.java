package com.eudemon.ratelimiter.exception;

public class ConfigurationResolveException extends RuntimeException {

  private static final long serialVersionUID = -5729317076315201572L;

  public ConfigurationResolveException(String message) {
    super(message);
  }

  public ConfigurationResolveException(String message, Throwable e) {
    super(message, e);
  }

}

