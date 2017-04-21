package com.yirendai.oss.environment.configserver;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by zhanghaolun on 16/9/13.
 */
public abstract class Security {

  public static final String USER_USER = "user";
  public static final String USER_WEBHOOK = "webhook";
  public static final String ADMIN = "ADMIN";
  public static final String USER = "USER";
  public static final String WEBHOOK = "WEBHOOK";
  public static final String ROLE_ADMIN = "ROLE_ADMIN";
  public static final String ROLE_USER = "ROLE_USER";
  public static final String ROLE_WEBHOOK = "ROLE_WEBHOOK";
  public static final String DELIMITER = "_-_";

  private Security() {
  }

  public static String modifyUsername(final String application, final String username) {
    checkArgument(!isAnyBlank(application, username), "application or username should not blank");

    return username + DELIMITER + application;
  }

  public static String restoreUsername(final String modifiedUsername) {
    checkArgument(isNotBlank(modifiedUsername), "username should not blank");

    final String[] splited = modifiedUsername.split(DELIMITER);
    return splited.length > 1 ? splited[1] : modifiedUsername;
  }
}
