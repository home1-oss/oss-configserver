package com.yirendai.oss.environment.configserver;

//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.hamcrest.MatcherAssert.assertThat;

import com.yirendai.oss.environment.configserver.util.HttpUtil;
import com.yirendai.oss.environment.configserver.util.TestEnv;
//import org.testng.annotations.DataProvider;
//import org.testng.annotations.Test;

/**
 * Created by leo on 16/9/20.
 */
public class DecryptTest {

  ////@Test(dataProvider="testDecryptConfigData")
  //public void testDecryptConfig(String comment, String userName, String password,
  //    String uri, String encryptedString,
  //    String expected) {
  //  String url = TestEnv.getUrl(uri);
  //  String result = HttpUtil.post(userName, password, url, encryptedString);
  //  //assertThat(result, equalTo(expected));
  //}

  //@DataProvider
  public static Object[][] testDecryptConfigData() {
    String encrpytedString = "AQAcBZjNSNIT4dFJR0mzqzVOVY2OsKim3UQyei7TXZ+VCaBVHKEX2ztFwAMaZr7LABZYAkJG/3+tfnrQoA4NsQGH0YybIMui55cyQCbMtaItRlzy9uegnRwJ5w4XOqJVdglthpqNldeKt2dxXj/C1UnHijvNWjZ+BnDc7b9mTgt4pi7dLHfaLD3tuddvRDrYiaR4oNDFn7qkEz52Jk3ooYhomr+O5QH6VTqQcVqmOJF54XPiFCFoMho9m115BHaLvqL02g26hirFuDd2+JqFXo6mxFpRHZeOKeqUKQFdIDYQarmiLp21RL4lYpao2ePtA4CKqDOwntC4zXtKHmA8NOosxtxRUAZ1Sdp9CPjur5Ws/A7+uSUC6TwLqCRGxTLq8dY=";
    return new Object[][]{
        {"验证只有管理员账号才能解密信息", "admin", "admin_pass", "/config/decrypt", encrpytedString, "mysecret"},
    };
  }

  //@Test(dataProvider="testUnauthorizedDecryptionData", expectedExceptions=HttpClientErrorException.class,expectedExceptionsMessageRegExp=".*Unauthorized.*")
  public void testUnauthorizedDecryption( //
      final String comment, //
      final String userName, //
      final String password, //
      final String uri, //
      final String encrpytedString //
  ) {
    final String url = TestEnv.getUrl(uri);
    HttpUtil.post(userName, password, url, encrpytedString);
  }

  //@DataProvider
  public static Object[][] testUnauthorizedDecryptionData() {
    String encrpytedString = "AQAcBZjNSNIT4dFJR0mzqzVOVY2OsKim3UQyei7TXZ+VCaBVHKEX2ztFwAMaZr7LABZYAkJG/3+tfnrQoA4NsQGH0YybIMui55cyQCbMtaItRlzy9uegnRwJ5w4XOqJVdglthpqNldeKt2dxXj/C1UnHijvNWjZ+BnDc7b9mTgt4pi7dLHfaLD3tuddvRDrYiaR4oNDFn7qkEz52Jk3ooYhomr+O5QH6VTqQcVqmOJF54XPiFCFoMho9m115BHaLvqL02g26hirFuDd2+JqFXo6mxFpRHZeOKeqUKQFdIDYQarmiLp21RL4lYpao2ePtA4CKqDOwntC4zXtKHmA8NOosxtxRUAZ1Sdp9CPjur5Ws/A7+uSUC6TwLqCRGxTLq8dY=";
    return new Object[][]{
        {"验证错误的账号和密码进行解密抛出Unauthorized异常信息", "admin", "admin", "/config/decrypt", encrpytedString},
    };
  }

  //@Test(dataProvider="testFalseEncryptedDecryptionData", expectedExceptions=HttpServerErrorException.class,expectedExceptionsMessageRegExp="500 Server Error")
  public void testFalseEncryptedDecryption( //
      String comment, //
      String userName, //
      String password, //
      String uri, //
      String encrpytedString //
  ) {
    final String url = TestEnv.getUrl(uri);
    HttpUtil.post(userName, password, url, encrpytedString);
  }

  //@DataProvider
  public static Object[][] testFalseEncryptedDecryptionData() {
    String falseEncrpytedString = "QAcBZjNSNIT4dFJR0mzqzVOVY2OsKim3UQyei7TXZ+VCaBVHKEX2ztFwAMaZr7LABZYAkJG/3" +
        "+tfnrQoA4NsQGH0YybIMui55cyQCbMtaItRlzy9uegnRwJ5w4XOqJVdglthpqNldeKt2dxXj/C1UnHijvNWjZ+BnDc7b9mTgt4pi7dLHfaLD3tuddvRDrYiaR4oNDFn7qkEz52Jk3ooYhomr+O5QH6VTqQcVqmOJF54XPiFCFoMho9m115BHaLvqL02g26hirFuDd2+JqFXo6mxFpRHZeOKeqUKQFdIDYQarmiLp21RL4lYpao2ePtA4CKqDOwntC4zXtKHmA8NOosxtxRUAZ1Sdp9CPjur5Ws/A7+uSUC6TwLqCRGxTLq8dY=";
    return new Object[][]{
        {"验证错误的加密信息解码时报500服务器错误", "admin", "admin_pass", "/config/decrypt", falseEncrpytedString},
    };
  }
}
