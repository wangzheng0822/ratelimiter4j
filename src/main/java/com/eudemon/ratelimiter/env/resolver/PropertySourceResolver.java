package com.eudemon.ratelimiter.env.resolver;

import java.io.InputStream;
import java.util.Map;

import com.eudemon.ratelimiter.exception.ConfigurationResolveException;

/**
 * The resolver is used to parse the configurations of different formats, such as yaml, properties.
 */
public interface PropertySourceResolver {

  String[] getSupportedFileExtensions();

  boolean canResolvedExtension(String fileExtension);

  Map<String, Object> resolve(InputStream in) throws ConfigurationResolveException;

}
