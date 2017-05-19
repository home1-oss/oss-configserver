package cn.home1.oss.environment.configserver;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.server.config.ConfigServerHealthIndicator;
import org.springframework.cloud.config.server.config.ConfigServerProperties;
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
  @ConditionalOnProperty(prefix = "spring.cloud.config.server.commonConfig.git", name = "uri")
  @EnableConfigurationProperties(value = {ConfigServerProperties.class, CommonConfigProperties.class})
  public static class OssGitRepositoryConfiguration {

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private ConfigServerProperties configServerProperties;

    @Autowired
    private CommonConfigProperties commonConfigProperties;

    @Bean
    @ConditionalOnMissingBean(EnvironmentRepository.class)
    public EnvironmentRepository environmentRepository() {
      // common-config
      final String label = this.commonConfigProperties.getLabel();
      final CommonConfigRepository commonConfigRepository = this.commonConfigProperties.getGit();
      commonConfigRepository.setApplication(this.commonConfigProperties.getApplication());
      commonConfigRepository.setLabel(isNotBlank(label) ? label : commonConfigRepository.getDefaultLabel());

      // multiple
      final CommonConfigSupportedMultipleJGitEnvironmentRepository repository =
          new CommonConfigSupportedMultipleJGitEnvironmentRepository(this.environment);
      repository.setCommonConfigRepository(commonConfigRepository);
      if (this.configServerProperties.getDefaultLabel() != null) {
        repository.setDefaultLabel(this.configServerProperties.getDefaultLabel());
      }

      return repository;
    }
  }
}
