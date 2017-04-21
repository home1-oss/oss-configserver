package com.yirendai.oss.environment.configserver;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.util.FS;

/**
 * Created by zhanghaolun on 16/9/18.
 */
public class CustomKeySshSessionFactory extends JschConfigSessionFactory {

  private String[] identityKeyPaths;

  public CustomKeySshSessionFactory(final String... identityKeyPaths) {
    this.identityKeyPaths = identityKeyPaths;
  }

  @Override
  protected void configure(final OpenSshConfig.Host hc, final Session session) {
    // nothing special needed here.
    session.setConfig("StrictHostKeyChecking", "no");
  }

  @Override
  protected JSch getJSch(final OpenSshConfig.Host hc, final FS fs) throws JSchException {
    final JSch jsch = super.getJSch(hc, fs);
    // Clean out anything 'default' - any encrypted keys that are loaded by default before this will break.
    jsch.removeAllIdentity();
    for (final String identKeyPath : this.identityKeyPaths) {
      jsch.addIdentity(identKeyPath);
    }
    return jsch;
  }
}
