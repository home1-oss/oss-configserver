package cn.home1.oss.environment.configserver;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepository;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class CommonConfigSupportedMultipleJGitEnvironmentRepository extends MultipleJGitEnvironmentRepository {

  private final String SPRING_APPLICATION_NAME_KEY = "spring.application.name";
  
  private final String PARENT_ENABLE_KEY = "spring.cloud.config.parent-config.enabled";
  private final String PARENT_APPLICATION_KEY = "spring.cloud.config.parent-config.application";
  private final String PARENT_LABEL_KEY = "spring.cloud.config.parent-config.label";
  private final String PARENT_PASSWORD_KEY = "spring.cloud.config.parent-config.password";
  
  @Value("${app.git.file.config.password.key:spring.cloud.config.password}")
  private String gitFileConfigPasswordKey;

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
      
      String parentAppName = CloudEnvironmentUtil.getByKey(applicationConfig, PARENT_APPLICATION_KEY);
      
      final String parentPassword = CloudEnvironmentUtil.getByKey(parentConfig, gitFileConfigPasswordKey);
      if (StringUtils.isNotBlank(parentPassword)) {
        String configPassword = CloudEnvironmentUtil.getByKey(applicationConfig, PARENT_PASSWORD_KEY);
        if (!parentPassword.equals(configPassword)) {
          throw new BadCredentialsException(
              "access parent password for '" + parentAppName + "' is empty or incorrect!");
        }
      }
      
      if (appNames.contains(parentAppName)) {
        log.warn("config server find parent loop! ignore succeed parent. application:{}, loop appName occured:{}",
            application, parentAppName);
        break;
      } else {
        appNames.add(parentAppName);
      }
      // application config first. revert cover.
      // PropertySources[n] will cover the value in PropertySources[n+1] with the same key.
      baseApplicationConfig.getPropertySources().addAll(parentConfig.getPropertySources());

    }

    return baseApplicationConfig;
  }


  private Environment getParentConfig(final Environment applicationConfig) {
    String enabledStr = CloudEnvironmentUtil.getByKey(applicationConfig, PARENT_ENABLE_KEY);
    if (enabledStr == null || !"true".equalsIgnoreCase(enabledStr.trim())) {
      return null;
    }
    String parentApplication = CloudEnvironmentUtil.getByKey(applicationConfig, PARENT_APPLICATION_KEY);
    String parentApplicationLabel = CloudEnvironmentUtil.getByKey(applicationConfig, PARENT_LABEL_KEY);
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
