package com.eudemon.ratelimiter.exception;

/**
 * The exception represents the api request overloads.
 */
public class OverloadException extends Exception {

  private static final long serialVersionUID = -6847906228777977513L;

  public OverloadException(String message) {
    super(message);
  }

  public OverloadException(String message, Throwable e) {
    super(message, e);
  }

}
