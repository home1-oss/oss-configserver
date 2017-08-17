package cn.home1.oss.environment.configserver;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.environment.EnvironmentController;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 *
 * config-server-client use the password configured in GIT. the user name must be the app name.
 *
 */
@Slf4j
@Service
public class GitFileConfigUserDetailsService implements UserDetailsService {

  @Autowired
  private EnvironmentController environmentController;

  @Autowired
  private Security security;

  @Value("${app.git.file.config.password.key:spring.cloud.config.password}")
  private String gitFileConfigPasswordKey;

  private final String PROFILE_NOT_EXIST = "_PROFILE_NOT_EXISTS_";

  @Override
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {

    if (username != null && username.equals(security.getAdminUserName())) {
      log.debug("struct admin user to memory");
      GrantedAuthority adminAuthority = new SimpleGrantedAuthority("ROLE_" + Role.ADMIN.toString());
      return new User(username, security.getAdminPassword(), Arrays.asList(adminAuthority));
    }

    if (username != null && username.equals(security.getWebHookUserName())) {
      log.debug("struct webhook user to memory");
      GrantedAuthority webhookAuthority = new SimpleGrantedAuthority("ROLE_" + Role.WEB_HOOK.toString());
      return new User(username, security.getWebHookUserPassword(), Arrays.asList(webhookAuthority));
    }

    // get environment without profile
    final Environment environment = environmentController.defaultLabel(username, PROFILE_NOT_EXIST);

    if (environment == null) {
      throw new UsernameNotFoundException("can not find the project with the name:" + username);
    }
    String password = CloudEnvironmentUtil.getByKey(environment, gitFileConfigPasswordKey);
    if (password == null) {
      throw new BadCredentialsException(
          "can not find the password (spring.cloud.config.password) from environment:" + environment.getName());
    }
    GrantedAuthority authority = new SimpleGrantedAuthority(Role.USER.toString());
    return new User(username, password, Arrays.asList(authority));

  }

}
