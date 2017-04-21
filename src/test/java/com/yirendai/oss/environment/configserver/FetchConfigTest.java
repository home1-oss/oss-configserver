package com.yirendai.oss.environment.configserver;


import com.yirendai.oss.environment.configserver.util.HttpUtil;
import com.yirendai.oss.environment.configserver.util.TestEnv;

//import org.testng.annotations.DataProvider;
//import org.testng.annotations.Test;

/**
 * Created by leo on 16/9/19.
 */
public class FetchConfigTest {

  //@Test(dataProvider="testConfigOverrideData")
  public void testConfigOverride(String comment, String uri) {
    String url = TestEnv.getUrl(uri);
    String result = HttpUtil.get(url);
    System.out.println(result);
  }

  //@DataProvider
  public static Object[][] testConfigOverrideData(){
    return new Object[][]{
        {"测试ci环境配置覆盖本项目公共配置", "/configserver-it/ci.env"},
        {"测试ci环境配置覆盖本项目公共配置", "/configserver-it/development.env"},
    };
  }
}
