package com.yirendai.oss.environment.configserver;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

import org.xbill.DNS.DClass;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Reverse DNS resolve.
 * see: http://archive.oreilly.com/pub/post/reverse_dns_lookup_and_java.html
 * Created by zhanghaolun on 16/11/2.
 */
@NoArgsConstructor(access = PRIVATE)
public abstract class ReverseDns {

  public static String reverseSystemDns(final String hostIp) throws UnknownHostException {
    final InetAddress addr = InetAddress.getByName(hostIp);
    return addr.getHostName(); // getCanonicalHostName()
  }

  public static String reverseJavaDns(final String hostIp) throws IOException {
    //final Record opt = null;
    final Resolver resolver = new ExtendedResolver();
    final Name name = ReverseMap.fromAddress(hostIp);
    final int type = Type.PTR;
    final int dclass = DClass.IN;
    final Record rec = Record.newRecord(name, type, dclass);
    final Message query = Message.newQuery(rec);

    final Message response = resolver.send(query);
    final Record[] answers = response.getSectionArray(Section.ANSWER);

    final String result;
    if (answers.length == 0) {
      result = hostIp;
    } else {
      result = answers[0].rdataToString();
    }
    return result;
  }
}
