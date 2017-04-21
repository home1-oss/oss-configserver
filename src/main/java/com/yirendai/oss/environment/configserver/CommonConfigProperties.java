package com.yirendai.oss.environment.configserver;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties of 'spring.cloud.config.server.commonConfig'.
 */
@ConfigurationProperties(prefix = "spring.cloud.config.server.commonConfig")
@Getter
@Setter
public class CommonConfigProperties {

  private boolean enabled;
  private String application = "application";
  private String label = "master";

  private CommonConfigRepository git;
}
