package cn.home1.oss.environment.configserver;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Created by zhanghaolun on 16/11/3.
 */
public class SecurityTest {

  @Test
  public void testModifyRestoreUsername() {
    final String application = "oss-todomvc-app";
    final String username = Security.USER_USER;
    final String modifiedUsername = Security.modifyUsername(application, username);
    assertEquals(username + Security.DELIMITER + application, modifiedUsername);

    final String restoredUsername = Security.restoreUsername(modifiedUsername);
    assertEquals(application, restoredUsername);

    assertEquals("adminUser", Security.restoreUsername("adminUser"));
  }
}
