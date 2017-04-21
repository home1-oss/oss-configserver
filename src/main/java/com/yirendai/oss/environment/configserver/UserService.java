package com.yirendai.oss.environment.configserver;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.collect.ImmutableMap;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserService {

  @Setter
  private PasswordEncoder passwordEncoder;
  @Setter
  private JdbcUserDetailsManager userDetailsManager;

  public List<Map<String, String>> getUsers() {
    final List<String> users = this.userDetailsManager.findUsersInGroup(Security.ROLE_USER);
    return users.stream() //
        .map(modifiedUsername -> ImmutableMap.of( //
            "username", Security.USER_USER, //
            "application", Security.restoreUsername(modifiedUsername) //
        )) //
        .collect(toList());
  }

  @Transactional
  public void createUser(final String application, final String username, final String password) {
    final String name = Security.modifyUsername(application, username);
    checkArgument(isNotBlank(password), "password should not blank");

    final List<GrantedAuthority> authorities = newArrayList(new SimpleGrantedAuthority(Security.ROLE_USER));
    final User user = new User(name, this.passwordEncoder.encode(password), authorities);
    this.userDetailsManager.createUser(user);
    this.userDetailsManager.addUserToGroup(name, Security.ROLE_USER);
  }

  @Transactional
  public void deleteUser(final String application, final String username, final Boolean checkExists) {
    final String name = Security.modifyUsername(application, username);
    checkState(this.userDetailsManager.userExists(name) || !checkExists, "user %s not exists.", username);

    this.userDetailsManager.removeUserFromGroup(name, Security.ROLE_USER);
    this.userDetailsManager.deleteUser(name);
  }

  public UserDetails getUser(final String application, final String username) {
    final String name = Security.modifyUsername(application, username);
    checkState(this.userDetailsManager.userExists(name), "user %s not exists.", username);

    final UserDetails userDetails = this.userDetailsManager.loadUserByUsername(name);
    final User user = new User(username, userDetails.getPassword(), userDetails.getAuthorities());
    user.eraseCredentials();
    return user;
  }

  @Transactional
  public void updateUser(final String application, final String username, final String password) {
    final String name = Security.modifyUsername(application, username);
    checkArgument(isNotBlank(password), "password should not blank");
    checkState(this.userDetailsManager.userExists(name), "user %s not exists.", username);

    final List<GrantedAuthority> authorities = newArrayList(new SimpleGrantedAuthority(Security.ROLE_USER));
    final User user = new User(name, this.passwordEncoder.encode(password), authorities);
    this.userDetailsManager.updateUser(user);
  }

  public Boolean authenticateUser(final String application) {
    final Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
    final String name = currentUser.getName();

    final UserDetails userDetails = this.userDetailsManager.loadUserByUsername(name);
    final Boolean result = userDetails != null;

    log.info("current user {} {} has application {}'s read permission.", //
        currentUser.getName(), result ? "" : "no", application);

    return result;
  }
}
