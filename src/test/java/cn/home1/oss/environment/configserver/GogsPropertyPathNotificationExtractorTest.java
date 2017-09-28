package cn.home1.oss.environment.configserver;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.junit.Assert.assertTrue;

import net.sf.json.JSONObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Map;

/**
 * 2017/1/19 yanzhang153
 */
public class GogsPropertyPathNotificationExtractorTest {
  private AnnotationConfigApplicationContext context;

  private GogsPropertyPathNotificationExtractor extractor;

  @Before
  public void setUp() {
    this.context = new AnnotationConfigApplicationContext();
    this.context.register(GogsPropertyPathNotificationExtractor.class);
  }

  @After
  public void teardown() {
    this.context.destroy();
  }

  @Test
  public void testGitPath() {
    EnvironmentTestUtils.addEnvironment(this.context, "spring.cloud.config.server.common-config.application:common");
    this.context.refresh();
    extractor = this.context.getBean(GogsPropertyPathNotificationExtractor.class);


    final MultiValueMap<String, String> headers = new HttpHeaders();
    headers.put("X-Gogs-Event", newArrayList("push"));

    assertTrue(contains( //
        this.extractor.extract(headers, payload("oss-todomvc-app-config")).getPaths(), //
        "oss*" //
    ));
    assertTrue(contains( //
        this.extractor.extract(headers, payload("home1-oss-common-config")).getPaths(), //
        "application" //
    ));
  }


  public Map payload(final String repository) {

    String url = "http://xxx/configserver/"
        + repository
        + "/commit/929f67f2b38a6269e7ad63f606c9d89a7d8eb79f";

    // gogs Request body
    String json = "{\n" +
        "    \"object_kind\": \"push\",\n" +
        "    \"before\": \"95790bf891e76fee5e1747ab589903a6a1f80f22\",\n" +
        "    \"after\": \"da1560886d4f094c3e6c9ef40349f7d38b5d27d7\",\n" +
        "    \"ref\": \"refs/heads/master\",\n" +
        "    \"checkout_sha\": \"da1560886d4f094c3e6c9ef40349f7d38b5d27d7\",\n" +
        "    \"user_id\": 4,\n" +
        "    \"user_name\": \"John Smith\",\n" +
        "    \"user_email\": \"john@example.com\",\n" +
        "    \"user_avatar\": \"https://s.gravatar.com/avatar/d4c74594d841139328695756648b6bd6?s=8://s.gravatar.com/avatar/d4c74594d841139328695756648b6bd6?s=80\",\n" +
        "    \"project_id\": 15,\n" +
        "    \"project\": {\n" +
        "        \"name\": \"Diaspora\",\n" +
        "        \"description\": \"\",\n" +
        "        \"web_url\": \"http://example.com/mike/diaspora\",\n" +
        "        \"avatar_url\": null,\n" +
        "        \"git_ssh_url\": \"git@example.com:mike/diaspora.git\",\n" +
        "        \"git_http_url\": \"http://example.com/mike/diaspora.git\",\n" +
        "        \"namespace\": \"Mike\",\n" +
        "        \"visibility_level\": 0,\n" +
        "        \"path_with_namespace\": \"mike/diaspora\",\n" +
        "        \"default_branch\": \"master\",\n" +
        "        \"homepage\": \"http://example.com/mike/diaspora\",\n" +
        "        \"url\": \"git@example.com:mike/diaspora.git\",\n" +
        "        \"ssh_url\": \"git@example.com:mike/diaspora.git\",\n" +
        "        \"http_url\": \"http://example.com/mike/diaspora.git\"\n" +
        "    },\n" +
        "    \"repository\": {\n" +
        "        \"name\": \"Diaspora\",\n" +
        "        \"url\": \"git@example.com:mike/diaspora.git\",\n" +
        "        \"description\": \"\",\n" +
        "        \"homepage\": \"http://example.com/mike/diaspora\",\n" +
        "        \"git_http_url\": \"http://example.com/mike/diaspora.git\",\n" +
        "        \"git_ssh_url\": \"git@example.com:mike/diaspora.git\",\n" +
        "        \"visibility_level\": 0\n" +
        "    },\n" +
        "    \"commits\": [\n" +
        "        {\n" +
        "            \"id\": \"b6568db1bc1dcd7f8b4d5a946b0b91f9dacd7327\",\n" +
        "            \"message\": \"Update Catalan translation to e38cb41.\",\n" +
        "            \"timestamp\": \"2011-12-12T14:27:31+02:00\",\n" +
        "            \"url\": \"" + url + "\",\n" +
        "            \"author\": {\n" +
        "                \"name\": \"Jordi Mallach\",\n" +
        "                \"email\": \"jordi@softcatala.org\"\n" +
        "            },\n" +
        "            \"added\": [\n" +
        "                \"CHANGELOG\"\n" +
        "            ],\n" +
        "            \"modified\": [\n" +
        "                \"app/controller/application.rb\"\n" +
        "            ],\n" +
        "            \"removed\": []\n" +
        "        },\n" +
        "        {\n" +
        "            \"id\": \"da1560886d4f094c3e6c9ef40349f7d38b5d27d7\",\n" +
        "            \"message\": \"fixed readme\",\n" +
        "            \"timestamp\": \"2012-01-03T23:36:29+02:00\",\n" +
        "            \"url\": \"" + url + "\",\n" +
        "            \"author\": {\n" +
        "                \"name\": \"GitLab dev user\",\n" +
        "                \"email\": \"gitlabdev@dv6700.(none)\"\n" +
        "            },\n" +
        "            \"added\": [\n" +
        "                \"CHANGELOG\"\n" +
        "            ],\n" +
        "            \"modified\": [\n" +
        "                \"app/controller/application.rb\"\n" +
        "            ],\n" +
        "            \"removed\": []\n" +
        "        }\n" +
        "    ],\n" +
        "    \"total_commits_count\": 4\n" +
        "}";
    JSONObject jasonObject = JSONObject.fromObject(json);
    Map<String, Object> map = (Map) jasonObject;
    return map;
  }

}