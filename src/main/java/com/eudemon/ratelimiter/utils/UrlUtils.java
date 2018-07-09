package com.eudemon.ratelimiter.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.eudemon.ratelimiter.exception.InvalidUrlException;

/**
 * Utils class for handle url: protocol://host:port/path?query(params)#fragment
 */
public class UrlUtils {

  /**
   * Split url path into segments. support the path with url template variable.
   * 
   * @param urlPath the url path
   * @return a list of path directories
   * @throws InvalidUrlException if the url is invalid
   */
  public static List<String> tokenizeUrlPath(String urlPath) throws InvalidUrlException {
    if (StringUtils.isBlank(urlPath)) {
      return Collections.emptyList();
    }

    if (!urlPath.startsWith("/")) {
      throw new InvalidUrlException("UrlParser tokenize error, invalid urls: " + urlPath);
    }

    String[] dirs = urlPath.split("/");
    List<String> dirlist = new ArrayList<String>();
    for (int i = 0; i < dirs.length; ++i) {
      if ((dirs[i].contains(".") || dirs[i].contains("?") || dirs[i].contains("*"))
          && (!dirs[i].startsWith("{") || !dirs[i].endsWith("}"))) {
        throw new InvalidUrlException("UrlParser tokenize error, invalid urls: " + urlPath);
      }

      if (!StringUtils.isEmpty(dirs[i])) {
        dirlist.add(dirs[i]);
      }
    }
    return dirlist;
  }

  /**
   * Get url path, remove parameters.
   *
   * {@literal "http://www.test.com/v1/user" -> "/v1/user" }
   * {@literal "/v1/user" --> "/v1/user" }
   * {@literal "/v1/user?lender=true"-->"/v1/user" }
   * 
   * @param url the invalid url
   * @return the path of the url
   * @throws InvalidUrlException if the url is invalid
   */
  public static String getUrlPath(String url) throws InvalidUrlException {
    if (StringUtils.isBlank(url)) {
      return null;
    }

    URI urlObj = null;
    try {
      urlObj = new URI(url);
    } catch (URISyntaxException e) {
      throw new InvalidUrlException("Get url path error: " + url, e);
    }

    String path = urlObj.getPath();
    if (path.isEmpty()) {
      return "/";
    }
    return path;
  }

  public static boolean validUrl(String url) {
    if (StringUtils.isBlank(url)) {
      return false;
    }
    try {
      new URL(url);
      return true;
    } catch (MalformedURLException e) {
      return false;
    }
  }

}
