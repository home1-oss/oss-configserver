package com.yirendai.oss.environment.configserver;

import static org.springframework.util.StringUtils.cleanPath;

import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.environment.JGitEnvironmentRepository;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.UrlResource;

import java.io.File;

/**
 * Git repository for common config shared by multiple application.
 */
public class CommonConfigRepository extends JGitEnvironmentRepository {

  private String application;
  private String label;

  public CommonConfigRepository(final ConfigurableEnvironment environment) {
    super(environment);
  }

  public CommonConfigRepository() {
    super(null);
  }

  public synchronized void setApplication(final String application) {
    this.application = application;
  }

  public synchronized void setLabel(final String label) {
    this.label = label;
  }

  public synchronized Environment findOne(final String profile) {
    return this.findOne(this.application, profile, this.label);
  }

  @Override
  protected File getWorkingDirectory() {
    File ret = null;
    if (getUri().startsWith("file:")) {
      try {
        ret = new UrlResource(cleanPath(getUri())).getFile();
      } catch (final Exception ex) {
        throw new IllegalStateException("Cannot convert uri to file: " + getUri(), ex);
      }
    }
    ret = ret == null ? getBasedir() : ret;
    return ret;
  }
}
