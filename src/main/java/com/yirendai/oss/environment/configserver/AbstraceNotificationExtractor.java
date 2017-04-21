package com.yirendai.oss.environment.configserver;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.monitor.PropertyPathNotificationExtractor;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 2017/1/17 yanzhang153
 */
public abstract class AbstraceNotificationExtractor implements PropertyPathNotificationExtractor {
  private static final Pattern PATH_PATTERN = Pattern.compile("^(https?)://(.+)/(.*)/commit/[a-zA-Z0-9]{7,40}");

  @Value("${spring.cloud.config.server.common-config.application}")
  private String commonApplicationName;

  protected void addAllPaths(final Set<String> paths, final Map<String, Object> commit, final String name) {
    //example:
    //http://gitlab.internal/configserver/oss-todomvc-app-config/commit/929f67f2b38a6269e7ad63f606c9d89a7d8eb79f
    final String url = (String) commit.get("url");
    if (StringUtils.isNotBlank(url)) {
      final Matcher matcher = PATH_PATTERN.matcher(url);
      if (matcher.matches()) {
        final String repository = matcher.group(3);
        final int lastIndex = repository.lastIndexOf('-');
        if (lastIndex > 0) {
          final String application = repository.substring(0, lastIndex);
          if (commonApplicationName.equals(application)) {
            paths.add("application");
          } else {
            paths.add(application);
          }
        }
      }
    }
  }
}
