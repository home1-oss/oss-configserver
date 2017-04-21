package com.yirendai.oss.environment.configserver;

import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 内嵌jetty配置.
 */
@Component
public class EmbededJettyConfig implements EmbeddedServletContainerCustomizer {
  @Value("${logging.path}")
  String logPath;

  /*
   * (non-Javadoc)
   *
   * @see org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer # customize
   */
  @Override
  public void customize(final ConfigurableEmbeddedServletContainer container) {
    // checks whether the container is Jetty
    if (container instanceof JettyEmbeddedServletContainerFactory) {
      ((JettyEmbeddedServletContainerFactory) container)
          .addServerCustomizers(jettyServerCustomizer());
    }
  }

  @Bean
  public JettyServerCustomizer jettyServerCustomizer() {

    return new JettyServerCustomizer() {

      /*
       * (non-Javadoc)
       *
       * @see org.springframework.boot.context.embedded.jetty.JettyServerCustomizer #
       * customize
       */
      @Override
      public void customize(final Server server) {

        // 配置access日志
        NCSARequestLog requestLog = new NCSARequestLog(logPath + "/access_log.log");
        requestLog.setExtended(false);

        RequestLogHandler requestLogHandler = new RequestLogHandler();
        requestLogHandler.setRequestLog(requestLog);
        requestLogHandler.setHandler(server.getHandler());
        server.setHandler(requestLogHandler);
      }
    };
  }
}