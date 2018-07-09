package com.eudemon.ratelimiter.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Order {

  int HIGHEST_PRECEDENCE = 0;

  int LOWEST_PRECEDENCE = 100;

  int value() default LOWEST_PRECEDENCE;
}
