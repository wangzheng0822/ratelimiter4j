package com.eudemon.ratelimiter.exception;

/**
 * The exception represents that the url is invalid.
 */
public class InvalidUrlException extends Exception {

  private static final long serialVersionUID = -518071766367132729L;

  public InvalidUrlException(String message) {
    super(message);
  }

  public InvalidUrlException(String message, Throwable e) {
    super(message, e);
  }

}
