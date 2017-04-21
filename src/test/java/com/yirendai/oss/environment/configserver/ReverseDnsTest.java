package com.yirendai.oss.environment.configserver;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

/**
 * Created by zhanghaolun on 16/11/2.
 */
@Slf4j
public class ReverseDnsTest {

  static {
    //System.setProperty("sun.net.spi.nameservice.nameservers", "10.141.7.50");
    //System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
  }

  /**
   * REVERSE DNS LOOKUP USING JNDI
   * In this example the IP being looked up is 10.141.4.186 The octets are reversed (186.4.141.10)
   * and appended to the in-addr.arpa zone: 186.4.141.10.in-addr.arpa
   */
  public static void reverseJndi() {
    try {
      final Hashtable<String, String> env = new Hashtable<>();
      env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");

      final DirContext ctx = new InitialDirContext(env);
      //final Attributes attrs = ctx.getAttributes("186.4.141.10", new String[]{"PTR"});
      final Attributes attrs = ctx.getAttributes("186.4.141.10.in-addr.arpa", new String[]{"PTR"});

      for (final NamingEnumeration ae = attrs.getAll(); ae.hasMoreElements(); ) {
        final Attribute attr = (Attribute) ae.next();
        final String attrId = attr.getID();
        for (final Enumeration vals = attr.getAll(); vals.hasMoreElements();
             System.out.println(attrId + ": " + vals.nextElement()))
          ;
      }
      ctx.close();
    } catch (Exception e) {
      log.info("NO REVERSE DNS");
    }
  }

  //@Test
  @SneakyThrows
  public void testReverseSystemDns() {
    reverseJndi();

    final String hostIp = "10.141.4.186";
    final long before = System.currentTimeMillis();
    final String hostName = ReverseDns.reverseSystemDns(hostIp);
    final long after = System.currentTimeMillis();
    log.info("reverseSystemDns hostIp: {}, hostName: {}, cost: {} ms", hostIp, hostName, after - before);
  }

  //@Test
  @SneakyThrows
  public void testReverseJavaDns() {
    final String hostIp = "10.141.4.186";
    final long before = System.currentTimeMillis();
    final String hostName = ReverseDns.reverseJavaDns(hostIp);
    final long after = System.currentTimeMillis();
    log.info("reverseJavaDns hostIp: {}, hostName: {}, cost: {} ms", hostIp, hostName, after - before);
  }
}
