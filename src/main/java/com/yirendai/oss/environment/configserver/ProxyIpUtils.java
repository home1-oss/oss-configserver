package com.yirendai.oss.environment.configserver;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * 获取代理请求的真实ip工具.
 * Created by Administrator on 2016/11/24.
 */
@Slf4j
public class ProxyIpUtils {

  private static final String X_REAL_IP_HEADER = "X-Real-IP";
  private static final String X_Forwarded_For_HEADER = "x-forwarded-for";

  /**
   * 获取http header中带过来的ip.
   *
   * @param req servletRequest
   * @return real IP from X-Real-IP header
   */
  public static String getRealIp(final ServletRequest req) {
    final HttpServletRequest request = (HttpServletRequest) req;
    final String realIp = request.getHeader(X_REAL_IP_HEADER);
    if (StringUtils.isNotEmpty(realIp)) {
      log.info("find http request realIp:{}", realIp);
    }

    return realIp;
  }


  /**
   * 获取代理转发的所有ip.
   *
   * @param req servletRequest
   * @return real IP from X-FORWARDED-FOR header
   */
  public static String getRealIpFromForwarded(final ServletRequest req) {
    final HttpServletRequest request = (HttpServletRequest) req;
    final String forwardedFor = request.getHeader(X_Forwarded_For_HEADER);
    if (StringUtils.isNotEmpty(forwardedFor)) {
      String[] ipList = forwardedFor.split(",");
      return ipList[0];
    }
    return null;
  }
}
