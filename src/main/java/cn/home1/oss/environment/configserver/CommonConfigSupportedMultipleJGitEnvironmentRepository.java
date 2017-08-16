package cn.home1.oss.environment.configserver;

import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepository;
import org.springframework.core.env.ConfigurableEnvironment;

@ConfigurationProperties("spring.cloud.config.server.git")
public class CommonConfigSupportedMultipleJGitEnvironmentRepository extends MultipleJGitEnvironmentRepository {

  @Setter
  private CommonConfigRepository commonConfigRepository;

  public CommonConfigSupportedMultipleJGitEnvironmentRepository(final ConfigurableEnvironment environment) {
    super(environment);
  }


  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    if (this.commonConfigRepository != null) {
      this.commonConfigRepository.setEnvironment(getEnvironment());
    }
  }

  @Override
  public Environment findOne(final String application, final String profile, final String label) {

    final Environment applicationConfig = super.findOne(application, profile, label);

    final Environment commonConfig = this.commonConfigRepository.findOne(profile);
    // application config first. revert cover.
    // PropertySources[n] will cover the value in PropertySources[n+1] with the same key.
    applicationConfig.getPropertySources().addAll(commonConfig.getPropertySources());

    return applicationConfig;
  }
}
