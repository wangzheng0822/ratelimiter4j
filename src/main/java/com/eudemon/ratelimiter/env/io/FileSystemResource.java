package com.eudemon.ratelimiter.env.io;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The class represent the resource in the file system.
 */
public class FileSystemResource implements Resource {

  private final File file;

  private final String path;

  public FileSystemResource(String path) {
    this.file = new File(path);
    this.path = path;
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
    return "file [" + this.file.getAbsolutePath() + "]";
  }

  @Override
  public boolean exists() {
    return this.file.exists();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new FileInputStream(this.file);
  }

}
