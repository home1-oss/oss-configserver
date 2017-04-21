package com.yirendai.oss.environment.configserver.util;

import static com.yirendai.oss.lib.common.BasicAuthUtils.BASIC_AUTH_HEADE_NAME;
import static com.yirendai.oss.lib.common.BasicAuthUtils.basicAuthHeader;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Created by leo on 16/9/20.
 */
public class HttpUtil {
  public static String get(String url) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add(BASIC_AUTH_HEADE_NAME, basicAuthHeader("admin", "admin_pass"));
    HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
    ResponseEntity rssResponse = restTemplate.exchange(
        url,
        HttpMethod.GET,
        requestEntity,
        String.class);
    return (String) rssResponse.getBody();
  }

  public static String post(String userName, String password, String url, String encrpytedString) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add(BASIC_AUTH_HEADE_NAME, basicAuthHeader(userName, password));
    HttpEntity requestEntity = new HttpEntity(encrpytedString, requestHeaders);
    ResponseEntity rssResponse = restTemplate.exchange(
        url,
        HttpMethod.POST,
        requestEntity,
        String.class);
    return (String) rssResponse.getBody();
  }

  /**
   * post for MediaType.APPLICATION_FORM_URLENCODED.
   */
  public static String post(String adminName, String adminPassword, String url, String projectPath, String
      userPassword) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    requestHeaders.add(BASIC_AUTH_HEADE_NAME, basicAuthHeader(adminName, adminPassword));
    HttpEntity httpEntity = new HttpEntity(userPassword, requestHeaders);
    ResponseEntity responseEntity = restTemplate.exchange(
        url,
        HttpMethod.POST,
        httpEntity,
        String.class
    );

    return (String) responseEntity.getBody();
  }

  public static String get(String adminName, String adminPassword, String url) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add(BASIC_AUTH_HEADE_NAME, basicAuthHeader(adminName, adminPassword));
    HttpEntity httpEntity = new HttpEntity(requestHeaders);
    ResponseEntity responseEntity = restTemplate.exchange(
        url,
        HttpMethod.GET,
        httpEntity,
        String.class
    );

    return (String) responseEntity.getBody();
  }

  public static String delete(String adminName, String adminPassword, String url) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add(BASIC_AUTH_HEADE_NAME, basicAuthHeader(adminName, adminPassword));
    HttpEntity httpEntity = new HttpEntity(requestHeaders);
    ResponseEntity responseEntity = restTemplate.exchange(
        url,
        HttpMethod.DELETE,
        httpEntity,
        String.class
    );

    return (String) responseEntity.getBody();
  }

  public static String update(String adminName, String adminPassword, String url, String newPassword) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    requestHeaders.add(BASIC_AUTH_HEADE_NAME, basicAuthHeader(adminName, adminPassword));
    HttpEntity httpEntity = new HttpEntity(newPassword, requestHeaders);
    ResponseEntity responseEntity = restTemplate.exchange(
        url,
        HttpMethod.PUT,
        httpEntity,
        String.class
    );

    return (String) responseEntity.getBody();
  }
}
