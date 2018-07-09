package com.eudemon.ratelimiter.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.eudemon.ratelimiter.exception.ConfigurationResolveException;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonUtils {

  private static ObjectMapper objectMapper =
      new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

  public static final String toJsonString(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ConfigurationResolveException("parse json failed.", e);
    }
  }

  public static final <T> String toJsonString(Object object, Class<T> serClazz,
      JsonSerializer<T> jsonSerializer) {
    try {
      objectMapper.registerModule(new SimpleModule().addSerializer(serClazz, jsonSerializer));
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ConfigurationResolveException("parse json failed.", e);
    }
  }

  public static <T> List<T> toList(String json, Class<T> clazz) {
    try {
      return objectMapper.readValue(json,
          objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    } catch (IOException e) {
      throw new ConfigurationResolveException("parse json failed.", e);
    }
  }

  public static <T> T json2Object(String json, Class<T> clazz) {
    if (StringUtils.isBlank(json)) {
      return null;
    }

    try {
      return objectMapper.readValue(json, clazz);
    } catch (Exception e) {
      throw new ConfigurationResolveException("parse json failed.", e);
    }
  }

  public static <T> T stream2Object(InputStream in, Class<T> clazz) {
    if (in == null) {
      return null;
    }

    try {
      return objectMapper.readValue(in, clazz);
    } catch (Exception e) {
      throw new ConfigurationResolveException("parse json failed.", e);
    }
  }

}
