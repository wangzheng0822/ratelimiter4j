package com.eudemon.ratelimiter.env.io;

/**
 * The interface represent a resource loader to load resource from specified location.
 */
public interface ResourceLoader {

	/** Pseudo URL prefix for loading from the class path: "classpath:" */
	public static final String CLASSPATH_URL_PREFIX = "classpath:";

	/** URL prefix for loading from the file system: "file:" */
	public static final String FILE_URL_PREFIX = "file:";

	Resource getResource(String location);

}
