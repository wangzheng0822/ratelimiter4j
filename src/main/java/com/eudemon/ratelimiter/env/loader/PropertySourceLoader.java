package com.eudemon.ratelimiter.env.loader;

import com.eudemon.ratelimiter.env.PropertySource;

/**
 * The interface represents the environment configuration loaders.
 */
public interface PropertySourceLoader {

	PropertySource load();

}
