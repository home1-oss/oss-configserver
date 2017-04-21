package com.yirendai.oss.environment.configserver;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Boolean.TRUE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import com.google.common.collect.ImmutableMap;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RequestMapping("${spring.cloud.config.server.prefix:}/users")
@RestController
@Slf4j
public class UserContoller {

  @Autowired
  private UserService userService;

  @RequestMapping(value = "/login", method = {POST}, produces = {APPLICATION_JSON_VALUE})
  public Map<String, Object> login(final Principal principal) {
    final Map<String, Object> result;
    if (principal != null && UsernamePasswordAuthenticationToken.class.isAssignableFrom(principal.getClass())) {
      final UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) principal;
      if (authenticationToken.isAuthenticated() && //
          authenticationToken.getPrincipal() != null && //
          User.class.isAssignableFrom(authenticationToken.getPrincipal().getClass())) {
        final User user = (User) authenticationToken.getPrincipal();
        final String modifiedUsername = user.getUsername();
        final String restoredUsername = Security.restoreUsername(modifiedUsername);
        checkArgument(!Security.USER_WEBHOOK.equals(restoredUsername), Security.USER_WEBHOOK + " is not allowed.");
        if (user.getAuthorities().contains(new SimpleGrantedAuthority(Security.ROLE_USER))) {
          result = ImmutableMap.of( //
              "username", Security.USER_USER, //
              "application", restoredUsername, //
              "authorities", user.getAuthorities() //
          );
        } else {
          result = ImmutableMap.of( //
              "username", restoredUsername, //
              "application", "*", //
              "authorities", user.getAuthorities() //
          );
        }
      } else {
        result = null;
      }
    } else {
      result = null;
    }
    return result;
  }

  @RequestMapping(value = "/", method = GET, produces = {APPLICATION_JSON_VALUE})
  public List<Map<String, String>> getUsers() {
    return this.userService.getUsers();
  }

  @RequestMapping(value = "/{application}/", method = POST, consumes = {APPLICATION_FORM_URLENCODED_VALUE})
  public void createUser( //
      final @PathVariable("application") String application, //
      final @RequestParam(name = "password") String password //
  ) {
    final String username = Security.USER_USER;
    this.userService.createUser(application, username, password);
  }

  @RequestMapping(value = "/{application}/", method = DELETE)
  public void deleteUser( //
      final @PathVariable("application") String application
  ) {
    final String username = Security.USER_USER;
    this.userService.deleteUser(application, username, TRUE);
  }

  @RequestMapping(value = "/{application}/", method = GET, produces = {APPLICATION_JSON_VALUE})
  public UserDetails getUser( //
      final @PathVariable("application") String application
  ) {
    final String username = Security.USER_USER;
    return this.userService.getUser(application, username);
  }

  @RequestMapping(value = "/{application}/", method = PUT, consumes = {APPLICATION_FORM_URLENCODED_VALUE})
  public void updateUser(
      final @PathVariable("application") String application, //
      final @RequestParam(name = "password") String password //
  ) {
    final String username = Security.USER_USER;
    this.userService.updateUser(application, username, password);
  }
}
