package com.yirendai.oss.environment.configserver;

import static com.google.common.collect.Lists.newArrayList;
import static com.yirendai.oss.lib.common.BasicAuthUtils.BASIC_AUTH_HEADE_NAME;

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Created by zhanghaolun on 16/9/14.
 */
public class AuthorizationHeaderWrapper extends HttpServletRequestWrapper {

  //private interface GetHeader {
  //  String getHeader(String name);
  //  Enumeration<String> getHeaders(String name);
  //}

  //@lombok.experimental.Delegate(excludes = GetHeader.class)
  private final HttpServletRequest request;

  private final String value;

  /**
   * Constructs a request object wrapping the given request.
   *
   * @throws IllegalArgumentException if the request is null
   */
  public AuthorizationHeaderWrapper(final HttpServletRequest request, final String value) {
    super(request);
    this.request = request;
    this.value = value;
  }

  @Override
  public String getHeader(final String name) {
    final String result;
    if (!BASIC_AUTH_HEADE_NAME.equals(name)) {
      result = this.request.getHeader(name);
    } else {
      result = this.value;
    }
    return result;
  }

  @Override
  public Enumeration<String> getHeaders(final String name) {
    final Enumeration<String> result;
    if (!BASIC_AUTH_HEADE_NAME.equals(name)) {
      result = this.request.getHeaders(name);
    } else {
      result = Collections.enumeration(newArrayList(this.value));
    }
    return result;
  }
}
