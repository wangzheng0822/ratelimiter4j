package com.eudemon.ratelimiter.env.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.reader.UnicodeReader;

import com.eudemon.ratelimiter.exception.ConfigurationResolveException;

/**
 * The yaml property source resolver to resolve the environment configuration in yaml format.
 */
public class YamlPropertySourceResolver extends AbstractPropertySourceResolver
    implements PropertySourceResolver {

  public YamlPropertySourceResolver() {
    super();
  }

  @Override
  public String[] getSupportedFileExtensions() {
    return new String[] {"yaml", "yml"};
  }

  @Override
  public Map<String, Object> resolve(InputStream in) throws ConfigurationResolveException {
    Yaml yaml = new Yaml();
    Map<String, Object> flattenedMap = new HashMap<String, Object>();
    Reader reader = null;
    try {
      reader = new UnicodeReader(in);
      for (Object object : yaml.loadAll(reader)) {
        if (object != null) {
          flattenedMap.putAll(getFlattenedMap(asMap(object)));
        }
      }
    } catch(YAMLException e) {
      throw new ConfigurationResolveException("parse yaml configuration failed.", e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          throw new ConfigurationResolveException("parse yaml configuration failed.", e);
        }
      }
    }
    return flattenedMap;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> asMap(Object object) {
    // YAML can have numbers as keys
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    if (!(object instanceof Map)) {
      return Collections.emptyMap();
    }

    Map<Object, Object> map = (Map<Object, Object>) object;
    for (Map.Entry<Object, Object> entry : map.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof Map) {
        value = asMap(value);
      }
      Object key = entry.getKey();
      if (key instanceof CharSequence) {
        result.put(key.toString(), value);
      } else {
        // It has to be a map key in this case
        result.put("[" + key.toString() + "]", value);
      }
    }
    return result;
  }

  /**
   * Return a flattened version of the given map, recursively following any nested Map or Collection
   * values. Entries from the resulting map retain the same order as the source.
   */
  private Map<String, Object> getFlattenedMap(Map<String, Object> source) {
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    buildFlattenedMap(result, source, null);
    return result;
  }

  private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source,
      String path) {
    for (Map.Entry<String, Object> entry : source.entrySet()) {
      String key = entry.getKey();
      if (hasText(path)) {
        if (key.startsWith("[")) {
          key = path + key;
        } else {
          key = path + "." + key;
        }
      }
      Object value = entry.getValue();
      if (value instanceof String) {
        result.put(key, value);
      } else if (value instanceof Map) {
        // Need a compound key
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) value;
        buildFlattenedMap(result, map, key);
      } else if (value instanceof Collection) {
        // Need a compound key
        @SuppressWarnings("unchecked")
        Collection<Object> collection = (Collection<Object>) value;
        int count = 0;
        for (Object object : collection) {
          buildFlattenedMap(result, Collections.singletonMap("[" + (count++) + "]", object), key);
        }
      } else {
        result.put(key, value != null ? value : "");
      }
    }
  }

  private boolean hasText(String str) {
    if (StringUtils.isEmpty(str)) {
      return false;
    }
    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }

}
