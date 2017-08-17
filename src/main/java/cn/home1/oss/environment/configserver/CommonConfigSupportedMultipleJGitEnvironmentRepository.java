package cn.home1.oss.environment.configserver;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepository;
import org.springframework.core.env.ConfigurableEnvironment;

@Slf4j
public class CommonConfigSupportedMultipleJGitEnvironmentRepository extends MultipleJGitEnvironmentRepository {

  private String commonEnalbeKey = "spring.cloud.config.common-config.enabled";
  private String commonApplicationKey = "spring.cloud.config.common-config.application";
  private String commonLabelKey = "spring.cloud.config.common-config.label";

  public CommonConfigSupportedMultipleJGitEnvironmentRepository(final ConfigurableEnvironment environment) {
    super(environment);
  }


  @Override
  public Environment findOne(final String application, final String profile, final String label) {

    final Environment applicationConfig = super.findOne(application, profile, label);

    final Environment commonConfig = getCommonConfig(applicationConfig);

    if (commonConfig != null) {
      // application config first. revert cover.
      // PropertySources[n] will cover the value in PropertySources[n+1] with the same key.
      applicationConfig.getPropertySources().addAll(commonConfig.getPropertySources());
    }

    return applicationConfig;
  }


  private Environment getCommonConfig(final Environment applicationConfig) {
    String enabledStr = CloudEnvironmentUtil.getByKey(applicationConfig, commonEnalbeKey);
    if (enabledStr == null || !"true".equalsIgnoreCase(enabledStr.trim())) {
      return null;
    }
    String commonApplication = CloudEnvironmentUtil.getByKey(applicationConfig, commonApplicationKey);
    String commonApplicationLabel = CloudEnvironmentUtil.getByKey(applicationConfig, commonLabelKey);
    if (StringUtils.isBlank(commonApplication)) {
      throw new IllegalStateException(
          "configuration error!  spring.cloud.common-config.enabled=true but spring.cloud.common-config.application is empty!");
    }

    String[] profiles = applicationConfig.getProfiles();
    if (profiles == null || profiles.length == 0) {
      profiles = new String[] { "_PROFILE_NOT_EXISTS_" };
    }
    Environment result = null;
    for (String profile : profiles) {
      Environment temEnvironment = null;
      try {
        temEnvironment = super.findOne(commonApplication, profile, commonApplicationLabel);
      } catch (Exception exception) {
        log.error("fetch common config with exception! commonApplication:{}, profile:{}, commonApplicationLabel:{}",
            commonApplication, profile, commonApplicationLabel, exception);
        throw exception;
      }
      if (result == null) {
        result = temEnvironment;
      } else {
        result.getPropertySources().addAll(temEnvironment.getPropertySources());
      }
    }
    return result;
  }
}
