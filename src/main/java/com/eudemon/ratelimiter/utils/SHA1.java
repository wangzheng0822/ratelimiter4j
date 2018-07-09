package com.eudemon.ratelimiter.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

public class SHA1 {
  private static final String CHARSET_UTF8 = "UTF-8";
  private static final String CHARSET_GBK = "GBK";

  public static String sha1Hex(String source) {
    return sha1Hex(source, CHARSET_UTF8);
  }

  public static String sha1Hex(String source, String charset) {
    if (!StringUtils.equals(charset, CHARSET_GBK)) {
      charset = CHARSET_UTF8;
    }
    byte[] bSource;
    try {
      bSource = source.getBytes(charset);
    } catch (Exception ex) {
      bSource = source.getBytes();
    }
    return DigestUtils.sha1Hex(bSource);
  }
}
