package com.eudemon.ratelimiter.monitor;

import java.util.concurrent.atomic.LongAdder;

public class MetricsCounter {

  private final LongAdder[] counters;

  public MetricsCounter() {
    int length = MetricType.values().length;
    counters = new LongAdder[length];
    for (int i = 0; i < length; i++) {
      counters[i] = new LongAdder();
    }
  }

  public void increment(MetricType... types) {
    for (MetricType type : types) {
      getCounter(type).increment();
    }
  }

  public void add(MetricType type, long value) {
    getCounter(type).add(value);
  }

  public long sumAndReset(MetricType type) {
    return getCounter(type).sumThenReset();
  }

  private LongAdder getCounter(MetricType type) {
    return counters[type.ordinal()];
  }

}
