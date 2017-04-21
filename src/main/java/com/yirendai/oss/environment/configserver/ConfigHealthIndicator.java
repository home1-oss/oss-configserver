package com.yirendai.oss.environment.configserver;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.stereotype.Component;

/**
 * 配置中心启动健康检查,查看common配置是否可读.
 */
@Slf4j
@Component
public class ConfigHealthIndicator implements HealthIndicator {
  @Autowired
  private EnvironmentRepository repository;

  @Value("${spring.cloud.config.server.common-config.application}")
  private String application;
  //  @Value("${spring.cloud.config.server.defaultProfile}")
  private String profile = "default";
  @Value("${spring.cloud.config.server.common-config.label}")
  private String label;

  @Override
  public Health health() {
    Health.Builder builder = new Health.Builder().withDetail("application", this.application)
        .withDetail("profile", this.profile).withDetail("label", this.label).unknown();
    try {
      check();
      builder.up();
    } catch (final Exception ex) {
      log.warn("read common config {} error:{}", getEnvStr(), ex);
      builder.down().withException(ex);
    }

    return builder.build();
  }

  private void check() {
    final Environment environment = this.repository.findOne(this.application, this.profile, this.label);
    log.info("read common config:{},environment:{}", getEnvStr(), environment.toString());
  }

  private String getEnvStr() {
    return "application:" + this.application + ",profile:" + this.profile + ",label:" + this.label;
  }
}