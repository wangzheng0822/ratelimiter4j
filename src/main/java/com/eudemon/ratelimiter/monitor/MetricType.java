package com.eudemon.ratelimiter.monitor;

public enum MetricType {
  TOTAL, // total count of limit operations
  PASSED, // get an access token
  LIMITED, // overload
  TIMEOUT, // limit operation timeout
  EXCEPTION, // some internal error occurs
  DURATION // cost time of limit operation
}
