package com.eudemon.ratelimiter.env.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * The interface represent a resource.
 */
public interface Resource {

  /**
   * Get the resource absolute path.
   * 
   * @return the resource absolute path.
   */
  String getPath();

  /**
   * Get the resource extension.
   * 
   * @return the resource extension.
   */
  String getExtension();

  /**
   * Get a description for this resource, to be used for error output when working with the
   * resource.
   * 
   * @return the description.
   */
  String getDescription();

  /**
   * Check if the resource exists or not.
   * 
   * @return true if exists.
   */
  boolean exists();

  /**
   * Open an {@link InputStream} for the resource.
   * 
   * @return the input stream of the resource.
   * @throws IOException if occurs error when opens the input stream for the resource.
   */
  InputStream getInputStream() throws IOException;

}
