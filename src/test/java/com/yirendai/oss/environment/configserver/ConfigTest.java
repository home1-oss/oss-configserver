package com.yirendai.oss.environment.configserver;

import com.yirendai.oss.environment.configserver.test.ConfigServerClient;

//import org.testng.annotations.AfterSuite;
//import org.testng.annotations.BeforeSuite;
//import org.testng.annotations.Test;

import java.util.Properties;

/**
 * Integration it for configuration service of the config server.
 * Created by leo on 16/9/23.
 */
public class ConfigTest {
  /**
   * Start the config server locally and start the config server client.
   */
  //@BeforeSuite
  public void prepareTestContext() {
    Properties properties = new Properties();
    properties.put("GIT_PREFIX", "git@gitlab.internal:configserver");
    System.setProperties(properties);

    ConfigServer.main();
//    createUser();  //同步
    ConfigServerClient.main();
    //New ConfigurationClass负责启动web应用,并检查是否正常启动,之后由CountDownLatch控制
    //是否进行下一准备工作的执行。
    //CountDownLatch
  }

  /**
   * Stop the config server and config server client.
   * TODO: shut down the web app after the thread ended.
   */
  //@AfterSuite
  public void shutDownContext() {

  }
}
