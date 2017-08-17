package cn.home1.oss.environment.configserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.config.server.config.ConfigServerHealthIndicator;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
@ConditionalOnProperty(prefix = "spring.cloud.config.server.commonConfig", name = "enabled", havingValue = "true")
public class CommonConfigConfiguration {


  @Bean
  @ConditionalOnProperty(value = "spring.cloud.config.server.health.enabled", matchIfMissing = true)
  public ConfigServerHealthIndicator configServerHealthIndicator(EnvironmentRepository repository) {
    return new ConfigServerHealthIndicator(repository);
  }


  @Configuration
  @ConditionalOnMissingBean(EnvironmentRepository.class)
  public static class OssGitRepositoryConfiguration {

    @Autowired
    private ConfigurableEnvironment environment;

    @Bean
    @ConditionalOnMissingBean(EnvironmentRepository.class)
    public EnvironmentRepository environmentRepository() {

      final CommonConfigSupportedMultipleJGitEnvironmentRepository repository =
          new CommonConfigSupportedMultipleJGitEnvironmentRepository(this.environment);

      return repository;
    }
  }
}
