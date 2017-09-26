package cn.home1.oss.environment.configserver;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepository;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class CommonConfigSupportedMultipleJGitEnvironmentRepository extends MultipleJGitEnvironmentRepository {

  private final String SPRING_APPLICATION_NAME_KEY = "spring.application.name";
  
  private String parentEnalbeKey = "spring.cloud.config.parent-config.enabled";
  private String parentApplicationKey = "spring.cloud.config.parent-config.application";
  private String parentLabelKey = "spring.cloud.config.parent-config.label";

  public CommonConfigSupportedMultipleJGitEnvironmentRepository(final ConfigurableEnvironment environment) {
    super(environment);
  }


  @Override
  public Environment findOne(final String application, final String profile, final String label) {

    final Environment baseApplicationConfig = super.findOne(application, profile, label);

    final Set<String> appNames = new HashSet<>();
    appNames.add(CloudEnvironmentUtil.getByKey(baseApplicationConfig, SPRING_APPLICATION_NAME_KEY));


    for (Environment parentConfig, applicationConfig = baseApplicationConfig; //
        (parentConfig = getParentConfig(applicationConfig)) != null; //
        applicationConfig = parentConfig) {
      String appName = CloudEnvironmentUtil.getByKey(applicationConfig, parentApplicationKey);
      if (appNames.contains(appName)) {
        log.warn("config server find parent loop! ignore succeed parent. application:{}, loop appName occured:{}",
            application, appName);
        break;
      } else {
        appNames.add(appName);
      }
      // application config first. revert cover.
      // PropertySources[n] will cover the value in PropertySources[n+1] with the same key.
      baseApplicationConfig.getPropertySources().addAll(parentConfig.getPropertySources());

    }

    return baseApplicationConfig;
  }


  private Environment getParentConfig(final Environment applicationConfig) {
    String enabledStr = CloudEnvironmentUtil.getByKey(applicationConfig, parentEnalbeKey);
    if (enabledStr == null || !"true".equalsIgnoreCase(enabledStr.trim())) {
      return null;
    }
    String parentApplication = CloudEnvironmentUtil.getByKey(applicationConfig, parentApplicationKey);
    String parentApplicationLabel = CloudEnvironmentUtil.getByKey(applicationConfig, parentLabelKey);
    if (StringUtils.isBlank(parentApplication)) {
      throw new IllegalStateException(
          "configuration error!  spring.cloud.parent-config.enabled=true but spring.cloud.parent-config.application is empty!");
    }

    String[] profiles = applicationConfig.getProfiles();
    if (profiles == null || profiles.length == 0) {
      profiles = new String[] { "_PROFILE_NOT_EXISTS_" };
    }
    Environment result = null;
    for (String profile : profiles) {
      Environment temEnvironment = null;
      try {
        temEnvironment = super.findOne(parentApplication, profile, parentApplicationLabel);
      } catch (Exception exception) {
        log.error("fetch common config with exception! commonApplication:{}, profile:{}, commonApplicationLabel:{}",
            parentApplication, profile, parentApplicationLabel, exception);
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
