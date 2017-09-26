package cn.home1.oss.environment.configserver;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

import javax.annotation.PostConstruct;

@Slf4j
@Getter
@Component
public class Security {

  @Value("${security.user.name:admin}")
  private String adminUserName;

  @Value("${security.user.password:}")
  private String adminPassword;

  @Value("${security.user.logAdminPassowrd:true}")
  private boolean logAdminPassowrd;

  @Value("${app.webhook.userName:webhook}")
  private String webHookUserName;

  private String webHookUserPassword = UUID.randomUUID().toString();

  @PostConstruct
  private void init() {
    if (StringUtils.isBlank(adminPassword)) {
      adminPassword = UUID.randomUUID().toString();
    }
    log.info("init admin, username:{}, password:{}", adminUserName, logAdminPassowrd ? adminPassword : "*[protected]*");
  }
}
