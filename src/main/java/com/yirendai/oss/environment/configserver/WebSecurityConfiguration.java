package com.yirendai.oss.environment.configserver;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.FALSE;
import static java.util.stream.Collectors.toList;
import static org.springframework.boot.autoconfigure.security.SecurityProperties.ACCESS_OVERRIDE_ORDER;

import com.google.common.collect.Lists;

import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.JdbcUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

@Order(ACCESS_OVERRIDE_ORDER)
@Configuration
@EnableWebSecurity
@Slf4j
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Value("${spring.cloud.config.server.prefix:}")
  private String configServerPrefix;

  @Value("${spring.cloud.config.server.prefix:}/users/login")
  private String loginEndpoint;

  @Value("${management.context-path:}")
  private String managementContextPath;

  @Value("${spring.cloud.config.monitor.endpoint.path:}/monitor")
  private String monitorEndpoint;

  @Value("${spring.cloud.config.server.monitor.whitelist:}")
  private String monitorWhitelist;

  private String webhookPassword;

  @Autowired
  private Environment environment;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private SecurityProperties securityProperties;

  @Autowired
  private UserService userService;

  public WebSecurityConfiguration() {
    this.webhookPassword = UUID.randomUUID().toString();
  }

  @Override
  public void init(final WebSecurity web) throws Exception {
    if (this.isH2DataSource()) {
      web.ignoring().antMatchers("/h2-console/**", "/index.html", "/webjars/**"); // see: H2ConsoleSecurityConfigurer
    }
    super.init(web);
  }

  @Override
  protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
    final JdbcUserDetailsManagerConfigurer jdbcAuthentication = auth.jdbcAuthentication();
    jdbcAuthentication.passwordEncoder(this.passwordEncoder());
    jdbcAuthentication.dataSource(this.dataSource);

    final JdbcUserDetailsManager jdbcUserDetailsManager = jdbcAuthentication.getUserDetailsService();
    jdbcUserDetailsManager.setJdbcTemplate(this.jdbcTemplate);
    // setUserCache(new SpringCacheBasedUserCache(new ConcurrentMapCache(SpringCacheBasedUserCache.class.getName())));

    this.createGroups(jdbcAuthentication);
    this.createAdminUser(jdbcAuthentication);
    this.createWebhookUser(jdbcAuthentication);

    this.userService.setPasswordEncoder(this.passwordEncoder());
    this.userService.setUserDetailsManager(jdbcUserDetailsManager);

    if (this.isH2DataSource()) {
      final String password = System.getProperty("defaultPassword", "user_pass");

      this.userService.deleteUser("oss-todomvc-app", Security.USER_USER, FALSE);
      this.userService.createUser("oss-todomvc-app", Security.USER_USER, password);

      this.userService.deleteUser("oss-todomvc-thymeleaf", Security.USER_USER, FALSE);
      this.userService.createUser("oss-todomvc-thymeleaf", Security.USER_USER, password);

      this.userService.deleteUser("oss-todomvc-gateway", Security.USER_USER, FALSE);
      this.userService.createUser("oss-todomvc-gateway", Security.USER_USER, password);
    }
  }

  @Override
  protected void configure(final HttpSecurity http) throws Exception {
    http //
        .csrf().disable() //
        .addFilterBefore(this.usernameModifyFilter(), BasicAuthenticationFilter.class) //
        .addFilterBefore(this.monitorWhitelistFilter(), BasicAuthenticationFilter.class) //
        .authorizeRequests() //
        .antMatchers(this.configServerPrefix + "/encrypt", this.configServerPrefix + this.monitorEndpoint).permitAll()
        //.antMatchers("/decrypt").permitAll() //
        .antMatchers(this.configServerPrefix + "/decrypt").hasRole(Security.ADMIN) //
        .antMatchers(this.configServerPrefix + "/users/login").permitAll() //
        .antMatchers(this.configServerPrefix + "/users/*").hasRole(Security.ADMIN) //
        .anyRequest().authenticated() //
        .and() //
        .httpBasic();
  }

  private void createGroups(final JdbcUserDetailsManagerConfigurer jdbcAuthentication) {
    final JdbcUserDetailsManager userDetailsService = jdbcAuthentication.getUserDetailsService();

    final List<String> groups = userDetailsService.findAllGroups();
    if (!groups.contains(Security.ROLE_ADMIN)) {
      userDetailsService.createGroup(Security.ROLE_ADMIN, //
          newArrayList(new SimpleGrantedAuthority(Security.ROLE_ADMIN)));
    }
    if (!groups.contains(Security.ROLE_USER)) {
      userDetailsService.createGroup(Security.ROLE_USER, //
          newArrayList(new SimpleGrantedAuthority(Security.ROLE_USER)));
    }
    if (!groups.contains(Security.ROLE_WEBHOOK)) {
      userDetailsService.createGroup(Security.ROLE_WEBHOOK, //
          newArrayList(new SimpleGrantedAuthority(Security.ROLE_WEBHOOK)));
    }
  }

  private void createAdminUser(final JdbcUserDetailsManagerConfigurer jdbcAuthentication) {
    final JdbcUserDetailsManager userDetailsService = jdbcAuthentication.getUserDetailsService();

    final String username = this.securityProperties.getUser().getName();
    final String rawPassword = this.securityProperties.getUser().getPassword();
    final List<String> roles = this.securityProperties.getUser().getRole().stream() //
        .sorted().distinct().collect(toList());
    checkArgument(!Security.USER_USER.equals(username), //
        "can not use " + Security.USER_USER + " as admin username.");
    checkArgument(!Security.USER_WEBHOOK.equals(username), //
        "can not use " + Security.USER_WEBHOOK + " as admin username.");
    checkArgument(roles.contains(Security.ADMIN), //
        "user must has role '" + Security.ADMIN + "'");

    try {
      for (final String admin : userDetailsService.findUsersInGroup(Security.ROLE_ADMIN)) {
        userDetailsService.removeUserFromGroup(admin, Security.ROLE_ADMIN);
        userDetailsService.deleteUser(admin);
      }
      jdbcAuthentication.getUserDetailsService().deleteUser(username);
      jdbcAuthentication.withUser(username) //
          .password(this.passwordEncoder().encode(rawPassword)) //
          .roles(roles.stream().toArray(String[]::new));
      userDetailsService.addUserToGroup(username, Security.ROLE_ADMIN);
    } catch (final UsernameNotFoundException ex) {
      log.debug("username '{}' not found", username, ex);
      jdbcAuthentication.withUser(username) //
          .password(this.passwordEncoder().encode(rawPassword)) //
          .roles(roles.stream().toArray(String[]::new));
      userDetailsService.addUserToGroup(username, Security.ROLE_ADMIN);
    }
  }

  private void createWebhookUser(final JdbcUserDetailsManagerConfigurer jdbcAuthentication) {
    final JdbcUserDetailsManager userDetailsService = jdbcAuthentication.getUserDetailsService();

    final String username = Security.USER_WEBHOOK;
    final String rawPassword = this.webhookPassword;
    final List<String> roles = Lists.newArrayList(Security.WEBHOOK);
    try {
      userDetailsService.removeUserFromGroup(username, Security.ROLE_WEBHOOK);
      userDetailsService.deleteUser(username);
      jdbcAuthentication.withUser(username) //
          .password(this.passwordEncoder().encode(rawPassword)) //
          .roles(roles.stream().toArray(String[]::new));
      userDetailsService.addUserToGroup(username, Security.ROLE_WEBHOOK);
    } catch (final UsernameNotFoundException ex) {
      log.debug("username '{}' not found", username, ex);
      jdbcAuthentication.withUser(username) //
          .password(this.passwordEncoder().encode(rawPassword)) //
          .roles(roles.stream().toArray(String[]::new));
      userDetailsService.addUserToGroup(username, Security.ROLE_WEBHOOK);
    }
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  public MonitorWhitelistFilter monitorWhitelistFilter() {
    final MonitorWhitelistFilter filter = new MonitorWhitelistFilter();
    filter.setEnvironment(this.environment);
    filter.setMonitorEndpoint(this.monitorEndpoint);
    filter.setMonitorWhitelist(this.monitorWhitelist);
    filter.setWebhookPassword(this.webhookPassword);
    return filter;
  }

  public UsernameModifyFilter usernameModifyFilter() {
    final UsernameModifyFilter filter = new UsernameModifyFilter();
    filter.setAdminUsername(this.securityProperties.getUser().getName());
    filter.setConfigServerPrefix(this.configServerPrefix);
    filter.setEnvironment(this.environment);
    filter.setLoginEndpoint(this.loginEndpoint);
    filter.setManagementContextPath(this.managementContextPath);
    return filter;
  }

  @Bean
  public PlatformTransactionManager transactionManager() {
    return new DataSourceTransactionManager(this.dataSource);
  }

  private Boolean isH2DataSource() {
    final HikariDataSource hikariDataSource = (HikariDataSource) this.dataSource;
    return "org.h2.Driver".equals(hikariDataSource.getDriverClassName());
  }
}
