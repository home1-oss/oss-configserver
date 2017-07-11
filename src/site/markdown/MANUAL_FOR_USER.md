
# oss-configserver 用户手册

## HTTP access

1. 访问方式 http://user:pass@ip:port/application/default/master
   
   application：项目名

   profile：略  ,按逗号分隔

   lable：分支名 默认master
   
   /{application}/{profile}[/{label}]  
   /{application}-{profile}.yml
   /{label}/{application}-{profile}.yml
   /{application}-{profile}.properties
   /{label}/{application}-{profile}.properties

    APP_NAME="oss-todomvc-thymeleaf";
    APP_PASS="user_pass";
    
    curl -i -u user:${APP_PASS} -X GET "http://configserver.local:8888/config/${APP_NAME}/development.env"

## Get application config info

    APP_NAME="oss-todomvc-thymeleaf";
    APP_PASS="user_pass";

    curl -i -u user:${APP_PASS} -X GET "http://configserver.local:8888/config/${APP_NAME}/development.env"

#### Encrypt

  Permit all access.

    curl -i -X POST "http://configserver.local:8888/config/encrypt" -d 'mysecret'
    
  Got: 'AQAcBZjNSNIT4dFJR0mzqzVOVY2OsKim3UQyei7TXZ+VCaBVHKEX2ztFwAMaZr7LABZYAkJG/3+tfnrQoA4NsQGH0YybIMui55cyQCbMtaItRlzy9uegnRwJ5w4XOqJVdglthpqNldeKt2dxXj/C1UnHijvNWjZ+BnDc7b9mTgt4pi7dLHfaLD3tuddvRDrYiaR4oNDFn7qkEz52Jk3ooYhomr+O5QH6VTqQcVqmOJF54XPiFCFoMho9m115BHaLvqL02g26hirFuDd2+JqFXo6mxFpRHZeOKeqUKQFdIDYQarmiLp21RL4lYpao2ePtA4CKqDOwntC4zXtKHmA8NOosxtxRUAZ1Sdp9CPjur5Ws/A7+uSUC6TwLqCRGxTLq8dY='


### encrypt property value (via HTTP api)

请求configserver服务器的http接口操作

  1.加密 /encrypt   post方式          *** configserver密钥必须配置好以后才能用（keystrore或者encrypt.key配置其一）

    $ curl configserver.local:8888/config/encrypt -d mysecret   #加密 mysecret
    682bc583f4641835fa2db009355293665d2647dade3375c0ee201de2a49f7bda  #密文
  2.解密 /decryprt  post方式           *** configserver密钥也必须配置好，正常情况客户端不需解密，读到的配置已经是明文

    $ curl configserver.local:8888/config/decrypt -d 682bc583f4641835fa2db009355293665d2647dade3375c0ee201de2a49f7bda
    mysecret  #解密 返回明文


***加密功能***  
configserver提供对配置敏感信息加密的功能。 

1.访问***configserver***的服务
  Permit all access.

    curl -i -X POST "http://configserver.local:8888/config/encrypt" -d 'mysecret'
    
  Got: 'AQAcBZjNSNIT4dFJR0mzqzVOVY2OsKim3UQyei7TXZ+VCaBVHKEX2ztFwAMaZr7LABZYAkJG/3+tfnrQoA4NsQGH0YybIMui55cyQCbMtaItRlzy9uegnRwJ5w4XOqJVdglthpqNldeKt2dxXj/C1UnHijvNWjZ+BnDc7b9mTgt4pi7dLHfaLD3tuddvRDrYiaR4oNDFn7qkEz52Jk3ooYhomr+O5QH6VTqQcVqmOJF54XPiFCFoMho9m115BHaLvqL02g26hirFuDd2+JqFXo6mxFpRHZeOKeqUKQFdIDYQarmiLp21RL4lYpao2ePtA4CKqDOwntC4zXtKHmA8NOosxtxRUAZ1Sdp9CPjur5Ws/A7+uSUC6TwLqCRGxTLq8dY='

2.加密后的值上传到git即可，客户端***不用做修改***直接得到的就是明文


***解密功能***

1.访问***configserver***的服务,需要admin权限
 
  Need admin permission.

    SECURITY_USER_PASSWORD="admin_pass";
    
    curl -i -u admin:${SECURITY_USER_PASSWORD} -X POST "http://configserver.local:8888/config/decrypt" -d '{cipher}AQAcBZjNSNIT4dFJR0mzqzVOVY2OsKim3UQyei7TXZ+VCaBVHKEX2ztFwAMaZr7LABZYAkJG/3+tfnrQoA4NsQGH0YybIMui55cyQCbMtaItRlzy9uegnRwJ5w4XOqJVdglthpqNldeKt2dxXj/C1UnHijvNWjZ+BnDc7b9mTgt4pi7dLHfaLD3tuddvRDrYiaR4oNDFn7qkEz52Jk3ooYhomr+O5QH6VTqQcVqmOJF54XPiFCFoMho9m115BHaLvqL02g26hirFuDd2+JqFXo6mxFpRHZeOKeqUKQFdIDYQarmiLp21RL4lYpao2ePtA4CKqDOwntC4zXtKHmA8NOosxtxRUAZ1Sdp9CPjur5Ws/A7+uSUC6TwLqCRGxTLq8dY='
    # or
    curl -i -u admin:${SECURITY_USER_PASSWORD} -X POST "http://configserver.local:8888/config/decrypt" -d 'AQAcBZjNSNIT4dFJR0mzqzVOVY2OsKim3UQyei7TXZ+VCaBVHKEX2ztFwAMaZr7LABZYAkJG/3+tfnrQoA4NsQGH0YybIMui55cyQCbMtaItRlzy9uegnRwJ5w4XOqJVdglthpqNldeKt2dxXj/C1UnHijvNWjZ+BnDc7b9mTgt4pi7dLHfaLD3tuddvRDrYiaR4oNDFn7qkEz52Jk3ooYhomr+O5QH6VTqQcVqmOJF54XPiFCFoMho9m115BHaLvqL02g26hirFuDd2+JqFXo6mxFpRHZeOKeqUKQFdIDYQarmiLp21RL4lYpao2ePtA4CKqDOwntC4zXtKHmA8NOosxtxRUAZ1Sdp9CPjur5Ws/A7+uSUC6TwLqCRGxTLq8dY='
    
  Got 'mysecret'




6.***git配置更新后 使用post方式调用/refresh接口即可更新配置***

## Runtime update (refresh)

git上配置更新后，调用客户项目的 /refresh 即可更新配置(***项目启动类必须配置*** @RefreshScope 注解)

#### 配置热更新

see: [build-self-healing-distributed-systems-with-spring-cloud](http://www.infoworld.com/article/2925047/application-development/build-self-healing-distributed-systems-with-spring-cloud.html)

1. push new config to Git

2. send POST request to the /bus/refresh endpoint on Application A, This request triggers three events:

`curl -X POST -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/bus/refresh`
`curl -X GET -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/api/greeting`

2.1. Application A requests the latest configuration from the Config Server.
Any Spring Beans annotated with @RefreshScope are reinitialized with the new configuration.

2.2. Application A sends a message to the AMQP exchange indicating it has received a refresh event.

2.3. Other Applications (B, C, ...), participating on the Cloud Bus by listening to the appropriate AMQP queue,
receive the message and update their configuration in the same manner as Applications A.

AMQP based, Exchange: springCloudBus

    # server-side
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
      <exclusions>
          <exclusion>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-logging</artifactId>
          </exclusion>
      </exclusions>
    </dependency>
    # client-side
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-config</artifactId>
      <scope>runtime</scope>
    </dependency>
    <!-- see: https://github.com/spring-cloud/spring-cloud-bus -->
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-bus-amqp</artifactId>
      <scope>runtime</scope>
      <exclusions>
        <exclusion>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
      </exclusions>
    </dependency>

override PropertyPathNotificationExtractor
see: [push_notifications_and_spring_cloud_bus](http://cloud.spring.io/spring-cloud-static/Brixton.SR6/#_push_notifications_and_spring_cloud_bus)
