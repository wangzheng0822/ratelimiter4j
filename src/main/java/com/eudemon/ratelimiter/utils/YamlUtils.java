package com.eudemon.ratelimiter.utils;

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import com.eudemon.ratelimiter.exception.ConfigurationResolveException;

import java.io.InputStream;

public class YamlUtils {

  private static final String DEFAULT_SPILT = "\\\\n";

  private static final String SYSTEM_SEPARATOR = System.getProperty("line.separator");

  public static <T> T parse(String yamlText, Class<T> clazz) {
    if (StringUtils.isEmpty(yamlText)) {
      return null;
    }

    yamlText = yamlText.replaceAll(DEFAULT_SPILT, SYSTEM_SEPARATOR);
    Yaml yaml = new Yaml();
    try {
      return yaml.loadAs(yamlText, clazz);
    } catch (Exception e) {
      throw new ConfigurationResolveException("parse yaml failed.", e);
    }
  }

  public static <T> T parse(InputStream in, Class<T> clazz) {
    if (in == null) {
      return null;
    }

    Yaml yaml = new Yaml();
    try {
      return yaml.loadAs(in, clazz);
    } catch (Exception e) {
      throw new ConfigurationResolveException("parse yaml failed.", e);
    }
  }

}
