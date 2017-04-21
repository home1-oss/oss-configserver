package com.yirendai.oss.environment.configserver;

//import static org.hamcrest.CoreMatchers.describedAs;
//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.hamcrest.MatcherAssert.assertThat;

import com.yirendai.oss.environment.configserver.util.HttpUtil;
import com.yirendai.oss.environment.configserver.util.TestEnv;

//import org.testng.annotations.DataProvider;
//import org.testng.annotations.Test;

/**
 * Created by leo on 16/9/20.
 */
public class UserTest {

  /** User 管理
  创建用户：
  curl -i -u admin:admin_pass -X POST -H 'Content-Type: application/x-www-form-urlencoded;' \
   -d "password=user_pass" \
   "http://local-configserver:8888/config/users/configserver-test/"
  查询用户：
  curl -i -u admin:admin_pass -X GET "http://local-configserver:8888/config/users/configserver-test/"
  删除用户：
  curl -i -u admin:admin_pass -X DELETE "http://local-configserver:8888/config/users/configserver-test/"
  更新用户密码：
  curl -i -u admin:admin_pass -X PUT -H 'Content-Type: application/x-www-form-urlencoded;' \
   -d "password=user_pass_new" \
   "http://local-configserver:8888/config/users/configserver-test/"
  */


  //@Test(dataProvider="testAddUserData")
  public void testAddUser(String comment, String uri, String projectPath, String userPassword, String adminName, String
      adminPassword, String expected) {

    String url = TestEnv.getUrl(uri, projectPath);
    String post = HttpUtil.post(adminName, adminPassword, url, projectPath, userPassword);
    System.out.println(post);
  }

  //@DataProvider
  public static Object[][] testAddUserData(){
    return new Object[][]{
        {"添加用户", "/config/users", "/configserver-it/", "password=user_pass", "admin", "admin_pass", ""},
    };
  }

  //@Test(dataProvider="testGetUserData")
  public void testGetUser(String comment, String uri, String projectPath, String adminName, String
      adminPassword, String expected) {

    String url = TestEnv.getUrl(uri, projectPath);
    String post = HttpUtil.get(adminName, adminPassword, url);
    System.out.println(post);
  }

  //@DataProvider
  public static Object[][] testGetUserData(){
    return new Object[][]{
        {"添加用户", "/config/users", "/configserver-it/", "admin", "admin_pass", ""},
    };
  }

  //@Test(dataProvider="testDeleteUserData")
  public void testDeleteUser(String comment, String uri, String projectPath, String adminName, String
      adminPassword, String expected) {

    String url = TestEnv.getUrl(uri, projectPath);
    String result = HttpUtil.delete(adminName, adminPassword, url);
    System.out.println(result);
  }

  //@DataProvider
  public static Object[][] testDeleteUserData(){
    return new Object[][]{
        {"添加用户", "/config/users", "/configserver-it/", "admin", "admin_pass", ""},
    };
  }

  //@Test(dataProvider="testUpdateUserData")
  public void testUpdateUser(String comment, String uri, String projectPath, String adminName, String
      adminPassword, String newPassword, String expected) {

    String url = TestEnv.getUrl(uri, projectPath);
    String result = HttpUtil.update(adminName, adminPassword, url, newPassword);
    System.out.println(result);
  }

  //@DataProvider
  public static Object[][] testUpdateUserData(){
    return new Object[][]{
        {"添加用户", "/config/users", "/configserver-it/", "admin", "admin_pass", "password=user_pass_new", ""},
    };
  }
}
