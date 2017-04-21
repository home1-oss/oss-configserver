package com.yirendai.oss.environment.configserver.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is a embedded spring boot web application for integration it.
 */
@SpringBootApplication
//    (exclude = {ConfigServerBootstrapConfiguration.class})
@RestController
public class ConfigServerClient {

  static {
    System.setProperty("spring.cloud.config.enabled", "true");
  }

  @Autowired
  private ConfigurableEnvironment environment;

  public static void main(final String... args) {
    SpringApplication.run(ConfigServerClient.class, args);
  }


  /**
   * This method is a way to get the specific configuration from the web application.
   * @param propertyName: property name of the configuration.
   * @return
   */
  @RequestMapping("/show")
  public String getPropertyByName(final @RequestParam String propertyName) {
    return environment.getProperty(propertyName);
  }
}
