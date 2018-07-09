package com.eudemon.ratelimiter.rule;

import com.eudemon.ratelimiter.exception.InvalidUrlException;
import com.eudemon.ratelimiter.utils.UrlUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Trie tree to store {@link ApiLimit} for one app. This class is thread-safe.
 */
public class AppUrlRateLimitRule {

  private static final Logger logger = LoggerFactory.getLogger(AppUrlRateLimitRule.class);

  private Node root;

  public AppUrlRateLimitRule() {
    root = new Node("/");
  }

  public void addLimitInfo(ApiLimit apiLimit) throws InvalidUrlException {
    String urlPath = apiLimit.getApi();
    if (!urlPath.startsWith("/")) {
      throw new InvalidUrlException("the api is invalid: " + urlPath);
    }

    if (apiLimit.getApi().equals("/")) {
      root.setApiLimit(apiLimit);
      return;
    }

    List<String> pathDirs;
    pathDirs = UrlUtils.tokenizeUrlPath(apiLimit.getApi());
    if (pathDirs == null || pathDirs.isEmpty()) {
      logger.warn("url [{}] is parsed to be empty pathDirs.", apiLimit.getApi());
      return;
    }

    Node p = root;
    for (String pathDir : pathDirs) {
      ConcurrentHashMap<String, Node> children = p.getEdges();

      String pathDirPattern = pathDir;
      boolean isPattern = false;
      if (isUrlTemplateVariable(pathDir)) {
        pathDirPattern = getPathDirPatten(pathDir);
        isPattern = true;
      }
      Node newNode = new Node(pathDirPattern, isPattern);
      Node existedNode = children.putIfAbsent(pathDirPattern, newNode);
      if (existedNode != null) {
        p = existedNode;
      } else {
        p = newNode;
      }
    }
    p.setApiLimit(apiLimit);
    logger.info("url [{}] set limit info {}.", apiLimit.getApi(), apiLimit.toString());
  }

  public void addLimitInfos(Collection<ApiLimit> apiLimits) throws InvalidUrlException {
    logger.info("add limit rules: {}", apiLimits.toString());

    for (ApiLimit apiLimit : apiLimits) {
      addLimitInfo(apiLimit);
    }
  }

  /**
   * Get limit info for the url path.
   * 
   * TODO(zheng): validate urlPath first. 
   * TODO(zheng): handle the case: children has multiply matched dir patterns. 
   * TODO(zheng): cache Regex Pattern for performance
   * 
   * @param urlPath urlPath path
   * @return the limit info for the urlPath.
   * @throws InvalidUrlException if the url path is invalid.
   */
  public ApiLimit getLimitInfo(String urlPath) throws InvalidUrlException {
    if (StringUtils.isBlank(urlPath)) {
      return null;
    }

    if (urlPath.equals("/")) {
      return root.getApiLimit();
    }

    List<String> pathDirs;
    pathDirs = UrlUtils.tokenizeUrlPath(urlPath);
    if (pathDirs == null || pathDirs.isEmpty()) {
      logger.warn("url [{}] is parsed to be empty pathDirs.", urlPath);
      return null;
    }

    Node p = root;
    ApiLimit currentLimit = null;
    if (p.getApiLimit() != null) {
      currentLimit = p.getApiLimit();
    }
    for (String pathDir : pathDirs) {
      ConcurrentHashMap<String, Node> children = p.getEdges();
      Node matchedNode = children.get(pathDir);
      if (matchedNode == null) {
        for (Map.Entry<String, Node> entry : children.entrySet()) {
          Node n = entry.getValue();
          if (n.getIsPattern()) {
            boolean matched = Pattern.matches(n.getPathDir(), pathDir);
            if (matched) {
              matchedNode = n;
            }
          }
        }
      }

      if (matchedNode != null) {
        p = matchedNode;
        if (matchedNode.getApiLimit() != null) {
          currentLimit = matchedNode.getApiLimit();
        }
      } else {
        break;
      }
    }

    logger.info("url [{}] get limit info: {}.", urlPath, currentLimit);
    return currentLimit;
  }

  public static final class Node {

    private String pathDir;

    private boolean isPattern;

    private ConcurrentHashMap<String, Node> edges = new ConcurrentHashMap<>();

    private ApiLimit apiLimit;

    public Node() {}

    public Node(String pathDir) {
      this(pathDir, false);
    }

    public Node(String pathDir, boolean isPattern) {
      this.pathDir = pathDir;
      this.isPattern = isPattern;
    }

    public void setIsPattern(boolean isPattern) {
      this.isPattern = isPattern;
    }

    public boolean getIsPattern() {
      return isPattern;
    }

    public String getPathDir() {
      return pathDir;
    }

    public void setPathDir(String pathDir) {
      this.pathDir = pathDir;
    }

    public ConcurrentHashMap<String, Node> getEdges() {
      return edges;
    }

    public ApiLimit getApiLimit() {
      return apiLimit;
    }

    public void setApiLimit(ApiLimit apiLimit) {
      this.apiLimit = apiLimit;
    }
  }

  private boolean isUrlTemplateVariable(String pathDir) {
    return pathDir.startsWith("{") && pathDir.endsWith("}");
  }

  private String getPathDirPatten(String pathDir) {
    StringBuilder patternBuilder = new StringBuilder();
    int colonIdx = pathDir.indexOf(':');
    if (colonIdx == -1) {
      patternBuilder.append("(^[0-9]*$)"); // default url template variable pattern: ID
    } else {
      String variablePattern = pathDir.substring(colonIdx + 1, pathDir.length() - 1);
      patternBuilder.append('(');
      patternBuilder.append(variablePattern);
      patternBuilder.append(')');
    }
    return patternBuilder.toString();
  }

}
