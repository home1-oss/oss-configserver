package com.yirendai.oss.environment.configserver;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newHashSet;
import static com.yirendai.oss.lib.common.BasicAuthUtils.BASIC_AUTH_HEADE_NAME;
import static com.yirendai.oss.lib.common.BasicAuthUtils.basicAuthHeader;
import static com.yirendai.oss.lib.common.BasicAuthUtils.extractAndDecodeAuthHeader;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.net.InetAddresses;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Note: Not a bean, avoid auto pick-up.
 * Created by zhanghaolun on 16/11/2.
 */
@Slf4j
public class MonitorWhitelistFilter extends GenericFilterBean {

  private String credentialsCharset = "UTF-8";

  @Setter
  private String monitorEndpoint;
  @Setter
  private String webhookPassword;

  private Set<String> monitorWhitelist = newHashSet();

  public void setMonitorWhitelist(final String monitorWhitelist) {
    if (isNotBlank(monitorWhitelist)) {
      this.monitorWhitelist = Arrays.stream(monitorWhitelist.split("[ ]*,[ ]*")).collect(toSet());
    } else {
      this.monitorWhitelist = newHashSet();
    }
  }

  @Override
  public void doFilter( //
      final ServletRequest req, //
      final ServletResponse res, //
      final FilterChain chain //
  ) throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    final Boolean isMonitorRequest = this.isMonitorRequest(request);
    if (isMonitorRequest) {
      final String existingHeader = request.getHeader(BASIC_AUTH_HEADE_NAME);
      if (existingHeader != null) {
        final String[] tokens = extractAndDecodeAuthHeader(existingHeader, this.getCredentialsCharset(request));
        assert tokens.length == 2;
        final String username = tokens[0];
        if (log.isTraceEnabled()) {
          log.trace("username: {}, password: {}", username, "*");
        }
      }
      checkArgument(isBlank(existingHeader), "Basic auth header not allowed: " + existingHeader);

      // see: http://archive.oreilly.com/pub/post/reverse_dns_lookup_and_java.html

      final String remoteAddr = request.getRemoteAddr();
      final String remoteHost = request.getRemoteHost();
      final Boolean inWhiteList = this.isInWhitelist(remoteAddr) || this.isInWhitelist(remoteHost);

      Boolean realIpInWhiteList = true;

      // 判断是否是代理
      String realIp = ProxyIpUtils.getRealIp(req);
      if (StringUtils.isNotEmpty(realIp)) {
        realIpInWhiteList = this.isInWhitelist(realIp);
      }

      if (inWhiteList && realIpInWhiteList) {
        log.info("remoteAddr: {}, remoteHost: {}, realIp: {} in whitelist.", remoteAddr, remoteHost, realIp);
        final String headerValue = basicAuthHeader(Security.USER_WEBHOOK, this.webhookPassword);
        chain.doFilter(new AuthorizationHeaderWrapper(request, headerValue), response);
      } else {
        log.info("remoteAddr: {}, remoteHost: {} realIp: {} not in whitelist.", remoteAddr, remoteHost, realIp);
        chain.doFilter(request, response);
      }
    } else {
      chain.doFilter(request, response);
    }
  }

  @SneakyThrows
  private Boolean isInWhitelist(final String hostIpOrName) {
    final Boolean result;
    if (InetAddresses.isInetAddress(hostIpOrName)) { // ipv4 address
      result = this.monitorWhitelist.contains(hostIpOrName) //
          || this.monitorWhitelist.contains(ReverseDns.reverseJavaDns(hostIpOrName));
    } else {
      result = this.monitorWhitelist.contains(hostIpOrName);
    }
    return result;
  }

  private Boolean isMonitorRequest(final HttpServletRequest request) {
    final String requestUri = request.getRequestURI();
    return requestUri.startsWith(this.monitorEndpoint);
  }

  public void setCredentialsCharset(final String credentialsCharset) {
    Assert.hasText(credentialsCharset, "credentialsCharset cannot be null or empty");
    this.credentialsCharset = credentialsCharset;
  }

  protected String getCredentialsCharset(final HttpServletRequest httpRequest) {
    return this.credentialsCharset != null ? this.credentialsCharset : httpRequest.getCharacterEncoding();
  }
}
