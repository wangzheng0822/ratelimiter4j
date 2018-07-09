package com.eudemon.ratelimiter.env.resolver;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractPropertySourceResolver implements PropertySourceResolver {

  @Override
  public boolean canResolvedExtension(String fileExtension) {
    if (StringUtils.isEmpty(fileExtension)) {
      return false;
    }

    String[] extensions = getSupportedFileExtensions();
    for (String extension : extensions) {
      if (extension.equalsIgnoreCase(fileExtension)) {
        return true;
      }
    }
    return false;
  }

}
