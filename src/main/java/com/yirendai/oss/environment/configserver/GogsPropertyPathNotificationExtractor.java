package com.yirendai.oss.environment.configserver;

import org.springframework.cloud.config.monitor.PropertyPathNotification;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.MultiValueMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * support gogs push event
 * see: {@link org.springframework.cloud.config.monitor.PropertyPathEndpoint#guessServiceName(String)}
 * see: {@link org.springframework.cloud.config.monitor.GitlabPropertyPathNotificationExtractor}
 * 2017/1/17 yanzhang153
 */
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class GogsPropertyPathNotificationExtractor extends AbstraceNotificationExtractor {
  @Override
  public PropertyPathNotification extract(MultiValueMap<String, String> headers, Map<String, Object> request) {
    if ("push".equals(headers.getFirst("X-Gogs-Event"))) {
      if (request.get("commits") instanceof Collection) {
        Set<String> paths = new HashSet<>();
        @SuppressWarnings("unchecked")
        Collection<Map<String, Object>> commits = (Collection<Map<String, Object>>) request.get("commits");
        for (Map<String, Object> commit : commits) {
          addAllPaths(paths, commit, "added");
          addAllPaths(paths, commit, "removed");
          addAllPaths(paths, commit, "modified");
        }
        if (!paths.isEmpty()) {
          return new PropertyPathNotification(paths.toArray(new String[0]));
        }
      }
    }
    return null;
  }
}
