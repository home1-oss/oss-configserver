package com.yirendai.oss.environment.configserver;

import static com.google.common.base.Preconditions.checkArgument;
import static com.yirendai.oss.boot.autoconfigure.PathUtils.isManagementPath;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.replaceOnce;

import com.yirendai.oss.lib.common.BasicAuthUtils;

import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Note: Not a bean, avoid auto pick-up.
 * Created by zhanghaolun on 16/9/14.
 */
@Slf4j
public class UsernameModifyFilter extends GenericFilterBean {

  @Data
  public static final class ConfigCoords {

    private final String application;
    private final String label;
    private final String profile;
    private final String ext;
  }

  public static final Pattern PATH_PATTERN = Pattern.compile("^/([^/]+)/([^/]+)(/([^/]+)?)?");
  public static final Pattern FILE_PATTERN = Pattern.compile("^(/[^/]+)?/(.+)-(.+)\\.(json|properties|yaml|yml)");

  private String credentialsCharset = "UTF-8";

  @Setter
  private String adminUsername;
  @Setter
  private String configServerPrefix;
  private RequestMatcher loginRequestMatcher;
  @Setter
  private RequestMatcher managementRequestMatcher;

  public void setLoginEndpoint(final String loginEndpoint) {
    this.loginRequestMatcher = new AntPathRequestMatcher(loginEndpoint);
  }

  public void setManagementContextPath(final String managementContextPath) {
    this.managementRequestMatcher = request -> request != null //
        ? isManagementPath(managementContextPath, request.getServletPath()) : false;
  }

  @Override
  public void doFilter( //
      final ServletRequest req, //
      final ServletResponse res, //
      final FilterChain chain //
  ) throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    final String[] tokens = this.tokens(request);

    if (this.loginRequestMatcher != null && this.loginRequestMatcher.matches(request) && tokens != null) {
      final String applicationOrUsername = tokens[0];

      if (this.adminUsername.equals(applicationOrUsername)) { // admin
        chain.doFilter(request, response);
      } else {
        final String name = Security.modifyUsername(applicationOrUsername, Security.USER_USER);
        final String headerValue = BasicAuthUtils.basicAuthHeader(name, tokens[1]);
        chain.doFilter(new AuthorizationHeaderWrapper(request, headerValue), response);
      }
    } else if (!this.managementRequestMatcher.matches(request) && tokens != null) {
      final ConfigCoords configCoords = this.extractConfigCoords(request);
      final String application = configCoords.getApplication();

      final String username = tokens[0];

      if (Security.USER_USER.equals(username) && isNotBlank(application)) {
        final String name = Security.modifyUsername(application, Security.USER_USER);
        final String headerValue = BasicAuthUtils.basicAuthHeader(name, tokens[1]);
        chain.doFilter(new AuthorizationHeaderWrapper(request, headerValue), response);
      } else { // admin
        chain.doFilter(request, response);
      }
    } else {
      chain.doFilter(request, response);
    }
  }

  private String[] tokens(final HttpServletRequest request) {
    final String header = request.getHeader(BasicAuthUtils.BASIC_AUTH_HEADE_NAME);
    final boolean hasBasicAuthHeader = header != null && header.startsWith("Basic ");
    final String[] tokens;
    if (hasBasicAuthHeader) {
      tokens = BasicAuthUtils.extractAndDecodeAuthHeader(header, this.getCredentialsCharset(request));
      checkArgument(tokens.length == 2, "invalid basic auth header");
    } else {
      tokens = null;
    }
    return tokens;
  }

  public void setCredentialsCharset(final String credentialsCharset) {
    Assert.hasText(credentialsCharset, "credentialsCharset cannot be null or empty");
    this.credentialsCharset = credentialsCharset;
  }

  protected String getCredentialsCharset(final HttpServletRequest httpRequest) {
    return this.credentialsCharset != null ? this.credentialsCharset : httpRequest.getCharacterEncoding();
  }

  private ConfigCoords extractConfigCoords(final HttpServletRequest request) {
    final String servletPath = request.getServletPath();
    final String path = isNotBlank(this.configServerPrefix) ? //
        replaceOnce(servletPath, this.configServerPrefix, "") : //
        servletPath;
    return extractConfigCoords(path);
  }

  /**
   * /{application}/{profile}[/{label}]
   * /{application}-{profile}.yml
   * /{label}/{application}-{profile}.yml
   * /{application}-{profile}.properties
   * /{label}/{application}-{profile}.properties
   */
  public static ConfigCoords extractConfigCoords(final String path) {
    final Matcher fileMatcher = FILE_PATTERN.matcher(path);
    final Matcher pathMatcher = PATH_PATTERN.matcher(path);

    final String application;
    final String profile;
    final String label;
    final String ext;
    if (fileMatcher.matches()) {
      label = fileMatcher.group(1) != null ? fileMatcher.group(1).substring(1) : ""; // could be null if not found
      application = fileMatcher.group(2);
      profile = fileMatcher.group(3);
      ext = fileMatcher.group(4);
    } else if (pathMatcher.matches()) { // pathMatcher.find()
      application = pathMatcher.group(1);
      profile = pathMatcher.group(2);
      label = pathMatcher.group(3) != null ? pathMatcher.group(3).substring(1) : "";
      ext = "";
    } else {
      application = "";
      profile = "";
      label = "";
      ext = "";
    }
    return new ConfigCoords(application, label, profile, ext);
  }
}
