package com.eudemon.ratelimiter.env.io;

import org.apache.commons.lang3.StringUtils;

import com.eudemon.ratelimiter.utils.ClassUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The class used to represent the resource in the class path.
 */
public class ClassPathResource implements Resource {

  private final String path;

  private ClassLoader classLoader;

  public ClassPathResource(String path) {
    this(path, (ClassLoader) null);
  }

  public ClassPathResource(String path, ClassLoader classLoader) {
    String pathToUse = path;
    if (pathToUse.startsWith("/")) {
      pathToUse = pathToUse.substring(1);
    }
    this.path = pathToUse;
    this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
  }

  @Override
  public final String getPath() {
    return this.path;
  }

  @Override
  public String getExtension() {
    int pos = path.lastIndexOf('.');
    if (pos == -1) {
      return null;
    }

    String extension = path.substring(pos + 1);
    if (StringUtils.isEmpty(extension)) {
      return null;
    }
    return extension;
  }

  @Override
  public String getDescription() {
    StringBuilder builder = new StringBuilder("class path resource [");
    String pathToUse = path;
    if (pathToUse.startsWith("/")) {
      pathToUse = pathToUse.substring(1);
    }
    builder.append(pathToUse);
    builder.append(']');
    return builder.toString();
  }

  /**
   */
  @Override
  public boolean exists() {
    return (resolveURL() != null);
  }

  @Override
  public InputStream getInputStream() throws IOException {
    InputStream is;
    if (this.classLoader != null) {
      is = this.classLoader.getResourceAsStream(this.path);
    } else {
      is = ClassLoader.getSystemResourceAsStream(this.path);
    }
    if (is == null) {
      throw new FileNotFoundException(
          getDescription() + " cannot be opened because it does not exist");
    }
    return is;
  }

  private URL resolveURL() {
    if (this.classLoader != null) {
      return this.classLoader.getResource(this.path);
    } else {
      return ClassLoader.getSystemResource(this.path);
    }
  }

}
