package cn.home1.oss.environment.configserver;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import cn.home1.oss.lib.common.Jackson2Utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.InputStream;

@Controller
@EnableConfigServer
@EnableEurekaClient
@EnableTransactionManagement
@SpringBootApplication
public class ConfigServer {

  @Autowired
  private Environment environment;

  @RequestMapping(path = "/")
  @ResponseBody
  public String index() {
    return "welcome to home1-oss config server! visit https://github.com/home1-oss/oss-configserver for more info.";
  }

  public static void main(final String... args) {
    SpringApplication.run(ConfigServer.class, args);
  }

  @Configuration
  @Order(HIGHEST_PRECEDENCE)
  public static class DeployKeyConfiguration {

    @Value("${spring.cloud.config.server.deployKey}")
    public void setDeployKey(final String deployKey) {
      final String keyPath = getDeployKeyPath(deployKey);
      SshSessionFactory.setInstance(new CustomKeySshSessionFactory(keyPath));
    }
  }

  @SneakyThrows
  private static String getDeployKeyPath(final String deployKey) {
    final String deployKeyPath;
    if (deployKey.startsWith("classpath:")) {
      // see: http://www.baeldung.com/convert-input-stream-to-a-file
      final String fileName = StringUtils.replaceOnce(deployKey, "classpath:", "");
      final InputStream initialStream = ConfigServer.class.getClassLoader().getResourceAsStream(fileName);
      final String userHome = System.getProperty("user.home");
      final String dataDir = userHome + "/data/configserver";
      FileUtils.forceMkdir(new File(dataDir));
      final File targetFile = new File(dataDir + "/default_deploy_key");
      FileUtils.copyInputStreamToFile(initialStream, targetFile);
      deployKeyPath = targetFile.getPath();
    } else if (deployKey.startsWith("file:")) {
      deployKeyPath = StringUtils.replaceOnce(deployKey, "file:", "");
    } else {
      deployKeyPath = deployKey;
    }
    return deployKeyPath;
  }

  @Bean
  @ConditionalOnProperty(value = "spring.cloud.config.server.monitor.gitlabpath.enabled", havingValue = "true")
  public GitlabpathPropertyPathNotificationExtractor gitlabPropertyPathNotificationExtractor() {
    return new GitlabpathPropertyPathNotificationExtractor();
  }

  @Bean
  @ConditionalOnProperty(value = "spring.cloud.config.server.monitor.gitlabpath.enabled", havingValue = "true")
  public GogsPropertyPathNotificationExtractor gogsPropertyPathNotificationExtractor() {
    return new GogsPropertyPathNotificationExtractor();
  }

  @Autowired
  public void setObjectMapper(final ObjectMapper objectMapper) {
    Jackson2Utils.setupObjectMapper(this.environment, objectMapper); // for GrantedAuthority
  }
}
