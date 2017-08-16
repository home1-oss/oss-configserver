package cn.home1.oss.environment.configserver;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * check privilege by login user and project name in URL.
 *
 */
@Slf4j
@Component
public class PathProjectNameSecurity {

  @Autowired
  private Security security;

  public boolean checkProjectPrivilege(final String pathProjectName) {
    final Authentication currUser = SecurityContextHolder.getContext().getAuthentication();
    boolean result = false;
    String userName = null;

    if (currUser != null) {
      userName = currUser.getName();
      if (security.getAdminUserName().equals(userName) || security.getWebHookUserName().equals(userName)) {
        result = true;
      } else if (pathProjectName != null && pathProjectName.equals(userName)) {
        result = true;
      }
    }

    log.debug("current username:{} has {} privilege of path project:{}", userName, result ? "" : "no", pathProjectName);

    return result;
  }
}
