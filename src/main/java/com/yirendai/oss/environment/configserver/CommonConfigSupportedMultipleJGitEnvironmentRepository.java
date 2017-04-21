package com.yirendai.oss.environment.configserver;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepository;
import org.springframework.core.env.ConfigurableEnvironment;

@ConfigurationProperties("spring.cloud.config.server.git")
public class CommonConfigSupportedMultipleJGitEnvironmentRepository extends MultipleJGitEnvironmentRepository {

  @Autowired
  private UserService userService;
  @Setter
  private CommonConfigRepository commonConfigRepository;

  public CommonConfigSupportedMultipleJGitEnvironmentRepository(final ConfigurableEnvironment environment) {
    super(environment);
  }

  public void setUserService(final UserService userService) {
    synchronized (this) {
      this.userService = userService;
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    if (this.commonConfigRepository != null) {
      this.commonConfigRepository.setEnvironment(getEnvironment());
    }
  }

  /**
   * check permission then read common config.
   */
  @Override
  public Environment findOne(final String application, final String profile, final String label) {
    synchronized (this) {
      final boolean authenticated = this.userService.authenticateUser(application);

      final Environment result;
      if (authenticated) {
        final Environment applicationConfig = super.findOne(application, profile, label);
        final Environment commonConfig = this.commonConfigRepository.findOne(profile);
        //commonConfig.setLabel(applicationConfig.getLabel());
        //commonConfig.setName(applicationConfig.getName());
        //commonConfig.setProfiles(applicationConfig.getProfiles());
        //commonConfig.setVersion(applicationConfig.getVersion());
        //commonConfig.getPropertySources().addAll(applicationConfig.getPropertySources());
        // application config first
        applicationConfig.getPropertySources().addAll(commonConfig.getPropertySources());
        result = applicationConfig;
      } else {
        result = new Environment(application, profile);
      }

      return result;
    }
  }
}
