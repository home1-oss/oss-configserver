package cn.home1.oss.environment.configserver;

import static org.springframework.boot.autoconfigure.security.SecurityProperties.ACCESS_OVERRIDE_ORDER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;


@Order(ACCESS_OVERRIDE_ORDER)
@Configuration
@EnableWebSecurity
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

  @Autowired
  private GitFileConfigUserDetailsService gitFileConfigUserDetailsService;

  @Override
  public void init(final WebSecurity web) throws Exception {
    super.init(web);
  }

  @Override
  protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(gitFileConfigUserDetailsService).passwordEncoder(NoOpPasswordEncoder.getInstance());
  }

  @Override
  protected void configure(final HttpSecurity http) throws Exception {
    http //
        .csrf().disable() //
        .authorizeRequests() //
        .antMatchers(configServerPrefix + "/").permitAll()//
        .antMatchers(configServerPrefix + "/deployPublicKey").permitAll()//
        .antMatchers(this.configServerPrefix + "/encrypt", this.monitorEndpoint).permitAll()
        .antMatchers(this.configServerPrefix + "/decrypt").hasRole(Role.ADMIN.toString()) //
        .antMatchers(new String[] { //
            this.configServerPrefix + "/{name}/{profiles:.*[^-].*}", //
            this.configServerPrefix + "/{name}/{profiles}/{label:.*}", //
            this.configServerPrefix + "/{name}-{profiles}.properties", //
            this.configServerPrefix + "/{label}/{name}-{profiles}.properties", //
            this.configServerPrefix + "/{name}-{profiles}.json", //
            this.configServerPrefix + "/{label}/{name}-{profiles}.json", //
            this.configServerPrefix + "/{name}-{profiles}.yml", //
            this.configServerPrefix + "/{name}-{profiles}.yaml", //
            this.configServerPrefix + "/{label}/{name}-{profiles}.yml", //
            this.configServerPrefix + "/{label}/{name}-{profiles}.yaml", //
            this.configServerPrefix + "/{name}/{profile}/{label}/**", //
            this.configServerPrefix + "/{name}/{profile}/{label}/**",//
        }).access("@pathProjectNameSecurity.checkProjectPrivilege(#name)")//
        .anyRequest().denyAll() //
        .and() //
        .httpBasic();
  }

}
