package com.eudemon.ratelimiter.utils;

public class Precondition {

  public static final void assertNotNull(Object object, String message) {
    if (object == null) {
      throw new IllegalArgumentException(message);
    }
  }

}
