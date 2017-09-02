package cn.home1.oss.environment.configserver;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConfigServer.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class GitFileConfigUserDetailsServiceTest {

  @Autowired
  private GitFileConfigUserDetailsService gitFileConfigUserDetailsService;

  @LocalServerPort
  private int port;

  @Test
  public void test() {
    UserDetails userDetails = gitFileConfigUserDetailsService.loadUserByUsername("my-config-test");
    System.out.println(userDetails.getPassword());
    System.out.println(userDetails);
  }
}
