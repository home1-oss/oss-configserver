package cn.home1.oss.environment.configserver;

import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;

import java.util.List;
import java.util.Map;

public class CloudEnvironmentUtil {

  public static String getByKey(Environment environment, String key) {
    if (environment == null || key == null) {
      return null;
    }
    List<PropertySource> propertySources = environment.getPropertySources();
    if (propertySources == null) {
      return null;
    }
    for (PropertySource propertySource : propertySources) {
      String value = getFromPropertySourceByKey(propertySource, key);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  private static String getFromPropertySourceByKey(PropertySource propertySource, String key) {
    Map<?, ?> source = propertySource.getSource();
    if (source == null) {
      return null;
    }
    Object value = source.get(key);
    return value == null ? null : value + "";
  }

}
