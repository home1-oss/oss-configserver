[toc]


## oss-configserver

### 概述
`oss-configserver`是在 `Spring Cloud Config`基础上进行定制开发, 是集成了权限控制, 配置修改主动通知等功能, 一个简单完整, 运维友好的配置中心解决方案. 此方案主要以`spring cloud`项目为主, 同时兼容`非spring cloud`情形, 详情请参考 [Spring Cloud Config 文档](https://cloud.spring.io/spring-cloud-config/).

此项目意在通过一个配置中心集群, 在几乎不需要运维介入的情况下, 管理多个项目.

####  功能特点

- 权限控制 集成了`spring security`, 以项目为粒度进行安全校验, 保证配置安全.
- 运维友好 此架构几乎不需要运维. 用户名为项目名, 密码为开发自主配置加密后的密码. 任何修改开发自助完成. 仅在需要解密时, 可以登录管理员账户进行解密.
- 主动通知 在配置变更后, 可以通过 `Spring cloud bus` 主动通知客户端, 触发配置更新动作. 当前支持`gitlab` 和 `gogs`. 根据项目名称, 可以实现`单一通知`, `前缀通知`和`全部通知`三种通知模式.
- 开箱即用 简单配置以后, 通过 `mvn` 命令, 直接打包成 `docker` 镜像, 即可在 `docker` 环境下部署.

### 快速使用


主要以下步骤:

1. 启动 `eureka` 服务发现
2. 启动 `Spring cloud bus` 要用到的 `rabbit MQ` 消息总线服务
3. 启动 `gitlab` git 仓库服务
4. 在 `gitlab` 创建配置项目
5. 配置并启动 `home1-oss configserver` 服务端
6. 启动 `config server` 客户端, 并测试

下面我们就一步一步来完成.

> 此使用手册主要使用 `home1-oss` 里面相应项目来示例. 大家也可以使用其它替代品. 如果已经有服务, 可以略过对应启动过程.

> 请自行先安装好`docker`和`docker compose`, 并申请和配置`docker`镜像加速(参考阿里docker镜像:https://cr.console.aliyun.com/#/accelerator).

#### 启动 `eureka` 服务

本文使用 `home1-oss` 下面的 `oss-eureka`.
启动步骤:

1. 将 `oss-eureka` clone 到本地某个目录 : `git clone https://github.com/home1-oss/oss-eureka.git /path/to/store`
2. cd 到对应目录里面
3. 执行 `docker-compose up` 命令来启动 `eureka` 服务

#### 启动 `rabit mq` 服务

本文使用 `home1-oss` 下面的 `docker-cloudbus`.
启动步骤:

同 `oss-eureka` 启动过程, `docker-cloudbus`地址为: `https://github.com/home1-oss/docker-cloudbus.git`

#### 启动 `gitlab` git 仓库服务

本文使用 `home1-oss` 下面的 `docker-gitlab` .
启动步骤:

同 `oss-eureka` 启动过程, 有两个文件夹, 进入`gitlab`文件夹, docker-compose up 命令启动即可. `docker-cloudbus`地址为: `https://github.com/home1-oss/docker-gitlab.git`

> 涉及到更新, `gitlab` 这个过程会相对较慢, 如果已经有 `gitlab`, 可以用现成的.

#### 在 `gitlab` 创建配置项目

1. 打开 gitlab `http://localhost:10080/`, 使用 `user`/`user_pass` 登录.
2. `home1-oss` group 应该已经创建好.
3. 在 `home1-oss` group下, 创建`my-config-test-config` 项目. 
4. 在`my-config-test-config`项目下, 创建`application.yml`文件, 并添加以下内容:
```
spring:
  application.name: 'my-config-test' 
  cloud.config.password: 'my-config-test' # !!! 此处最好存放加密后内容, 其他人尽管看到, 也不知道密码. 详见本文档 加密 相关内容

message: hello, home1-oss configserver!

spring.rabbitmq:  # mq 相关配置, 后面会进一步描述
  host: cloudbus.local
  port: 5672
  username: user
  password: user_pass

```
5. 在项目中进入`settings`-`Repository`菜单,在`deploy key`里面, 会有一个名字为`configserver@home1.cn_xxx`的deployKey, 点击 `enable` 将其激活(每个项目需要单独激活). 这样 configerserver 就有权限访问到项目数据了. 如果使用已有 gitlab, 那么默认不会有这个`configserver@home1.cn_xxx` deploy key. 手工添加 deploy Key 见后面.

> 配置项目必须以 `-config` 结尾, 这是 `oss-configserver` 的强制要求.

##### 在 gitlab 中添加 deploy key
1. 获取 configserver deploy key, 执行以下命令`curl http://localhost:8888/config/deployPublicKey`
2. 在 gitlab 中以管理员身份点击`admin area`那个小扳手的图标. 进入管理员界面.
3. 点开齿轮状设置菜单, 选择`Deploy keys`子菜单.
4. 点击`New Deploy Key` 将刚才第一步获得的 deployKey 添加上即可. 最后去对应项目中, 将 deploy key 设置为可用状态.
```
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDJexpGshox4d2mRhYIjOjxlAmcF9k9fKzlr2ylKS32LwMrVeKY+XyV06YvX0FE0uwj3DSp2Vai2e8kEylRDhQmuV1ZjjA08P9/j9SacFuzY8TfncdUwsQ3wxmBjmlpQoODUad7v0ld0r1AfttqbfGJr8L5gPzxvoA96K+6PkYyzUwbStJiW0ruNEVOb5LgN/v90LWMorwXj2Y/fu+i5OWp+iCTrQ6ltC6xQ/f3MyRMbfUxW3cXNp9UkdVkFDJ4Le/5poim5yPi6d2vjG8z7h5hM7M+H7q72hVoH9Rx0yzp55jOSRMXDGU138pK6HQFU/mCw9yaT0OwGK5IdvaX+ryd configserver@home1.cn
```

#### 配置并启动 `home1-oss configserver` 服务端


1. 将项目克隆到本地:`https://github.com/home1-oss/oss-configserver.git`
2. 将以下加入到 `hosts` 文件中.
```
127.0.0.1    cloudbus.local
127.0.0.1    oss-eureka.local
127.0.0.1    gitlab.local
```
3. (如果完全按照本文档默认步骤操作, 则这几个选项不用修改). 修改 `src/main/resources/application.yml` 的 `eureka.instance`, `spring.rabbitmq` 和 `spring.cloud.config.server.git.uri` 对应节点.
4. 启动`home1-oss configserver`. cd 到对应目录里面, 执行: `mvn spring-boot:run`

#### 启动 `config server` 客户端, 并测试

- 创建客户端 maven 项目, 在 `pom`里面 parent 和 dependencies 配置如下
```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>cn.woo5.test</groupId>
    <artifactId>config-server-client-test</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.5.2.RELEASE</version>
        <relativePath /> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <start-class>cn.woo5.test.config_server_client_test.ConfigServerClientApp</start-class>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bus-amqp</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Dalston.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```
- 创建主类, 添加 controller 功能.
```
package cn.woo5.test.config_server_client_test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
@SpringBootApplication
public class ConfigServerClientApp {

  @Value("${message}")
  private String message;

  @RequestMapping("/message")
  String getMessage() {
    return this.message;
  }

  public static void main(String[] args) {
    SpringApplication.run(ConfigServerClientApp.class, args);
  }
}


```
- 创建客户端启动配置文件 (src/main/resources/bootstrap.yml)

```
spring.application.name: my-config-test  # 我们在 gitlab 上创建的项目为 my-config-test-config , 这里取 "-config" 前面的部分
spring.cloud.config.uri: http://localhost:8888/config # /config 是 oss-configserver 统一路径前缀
spring.cloud.config.username: my-config-test # username 必须和applicationName一致
spring.cloud.config.password: my-config-test
spring.cloud.config.label: master
spring.cloud.config.profile: development.env

management.security.enabled: false
```

- 启动客户端
```
mvn spring-boot:run
```

- 访问并获得结果
访问 `http://localhost:8080/message` 结果将会是:
```
hello, home1-oss configserver!
```

至此, 完成 config server 基本使用.

> 如果 configer server 服务端报错:`Caused by: com.jcraft.jsch.JSchException: timeout: socket is not established`. 这应该是本机 ssh 链接 gitlab 过慢导致. 可以在 application.yml 中配置 `spring.cloud.config.server.jgit.timeout` 把时间设置长一些. 单位秒.

> 直接访问 `http://my-config-test:my-config-test@localhost:8888/config/my-config-test/development.env` 可以得到 gitlab 上面的配置信息. 在访问时, 最好一直带着用户名密码, 因 config-server 在集群环境下, `session` 授权不共享.

### 配置加密解密

在 gitlab 中创建的配置项目是他人可见的, 我们可以将密码等敏感字段加密.

#### 加密

我们将密码以密文形式来存放.

获取密文
命令行输入: 
```
curl http://localhost:8888/config/encrypt -X POST -d 'my-config-test'
```
我们会获得以下返回字串:
```
AQB4hVdKfdq5m3/OUAot6cHsm0aBFnZ84MKBYoxplYKmyprJ0wmAHhjrYsytm1ItDR3Gtem6FLeqhkipRKPg2J+2dkmvcSWNi2qWz9dZ/fdPDnAdtI8g+mVwbrBn0y1wrwQyGMFlrW93biZJlNInSDtBJSX0FshPcv/p4E/p9RCw8IbuizI7d8O+Tr4CP2w21EUiQPDUQRB8BY0k3vqCULzOLvRTqnibgCcPsTk8+pZdYYNtCjuSbxcfrcogq2c1rrTKwbfWF4FjluKLTLfiobYNIkhASmagKq71LxpumJ5PwHR5FC1sEmv/mZsnNy09h36JYwF5zGbjog+Yu8wbVjosCsQWWg1gOliV8kkJ0BujELG956EtrTV+bm7m7AYzThA=
```
把密文前面加上`{cipher}` 字串以后, 加上单引号, gitlab 内容更新为:
```
spring:
  application.name: 'my-config-test' 
  cloud.config.password: '{cipher}AQB4hVdKfdq5m3/OUAot6cHsm0aBFnZ84MKBYoxplYKmyprJ0wmAHhjrYsytm1ItDR3Gtem6FLeqhkipRKPg2J+2dkmvcSWNi2qWz9dZ/fdPDnAdtI8g+mVwbrBn0y1wrwQyGMFlrW93biZJlNInSDtBJSX0FshPcv/p4E/p9RCw8IbuizI7d8O+Tr4CP2w21EUiQPDUQRB8BY0k3vqCULzOLvRTqnibgCcPsTk8+pZdYYNtCjuSbxcfrcogq2c1rrTKwbfWF4FjluKLTLfiobYNIkhASmagKq71LxpumJ5PwHR5FC1sEmv/mZsnNy09h36JYwF5zGbjog+Yu8wbVjosCsQWWg1gOliV8kkJ0BujELG956EtrTV+bm7m7AYzThA='

message: hello, home1-oss configserver!

spring.rabbitmq:  # mq 相关配置, 后面会进一步描述
  host: cloudbus.local
  port: 5672
  username: user
  password: user_pass
```

直接访问 http://my-config-test:my-config-test@localhost:8888/config/my-config-test/development.env 可以得到 gitlab 上面的配置信息. 密码已经被解密.

这样, 我们重启客户端, 同样可以用配置的 'my-config-test' 密码来访问项目.

> 注: 加密内容在 `yml` 文件中要加单引号, 在 `properties` 文件中不能加单引号, 要不然内容不会被解密.

#### 解密

> 解密需要管理员权限. 默认管理员用户名为 `admin` 密码速记生成并打印在日志中. 可以在 `application.yml` 中设置默认密码.

使用 `curl http://ADMIN_NAME:ADMIN_PASS@localhost:8888/config/decrypt -X POST -d 'data to decrypt'` 解密. 把刚才加密的内容解密, 示例如下:

```
wanghaodembp:~ wanghao$ curl http://admin:fb8ff2bf-eb79-4c96-a88f-3ca6ec702ec6@localhost:8888/config/decrypt -X POST -d 'AQB4hVdKfdq5m3/OUAot6cHsm0aBFnZ84MKBYoxplYKmyprJ0wmAHhjrYsytm1ItDR3Gtem6FLeqhkipRKPg2J+2dkmvcSWNi2qWz9dZ/fdPDnAdtI8g+mVwbrBn0y1wrwQyGMFlrW93biZJlNInSDtBJSX0FshPcv/p4E/p9RCw8IbuizI7d8O+Tr4CP2w21EUiQPDUQRB8BY0k3vqCULzOLvRTqnibgCcPsTk8+pZdYYNtCjuSbxcfrcogq2c1rrTKwbfWF4FjluKLTLfiobYNIkhASmagKq71LxpumJ5PwHR5FC1sEmv/mZsnNy09h36JYwF5zGbjog+Yu8wbVjosCsQWWg1gOliV8kkJ0BujELG956EtrTV+bm7m7AYzThA='
my-config-testwanghaodembp:~ wanghao$
```

> 注意: 上面这次示例中, fb8ff2bf-eb79-4c96-a88f-3ca6ec702ec6 是我本次运行时生成的随机密码.


### 配置通用配置(父配置)

我们在配置不同项目的时候, 往往很多配置是可以提取出来共用的. `oss-configserver` 支持此功能.

父配置中一个Key对应的内容会被子配置中相同Key对应内容覆盖.

> `oss-configserver`支持任意层级的继承.
> 但是注意, 
> 1. 限制继承层级以保持效率. 
> 2. 避免循环继承.
> 3. 继承关系项目最好要有统一前缀, 方便变更父项目, 子项目得到通知. 详见本文档: 主动通知 部分.

如果父配置设置了密码, 则子配置要继承需要提供密码才能访问(父文件中密码设置 key: spring.cloud.config.password. 子文件配置父密码Key: spring.cloud.config.parent-config.password). 

#### 通用配置示例

我们现在为`my-config-test-config`配置项目增加一个父配置项目.

- 在 gitlab 中创建 `my-config-test-common-config` 项目, 新建 `application.yml` 文件. 文件中添加:

```
message: this is father mesage
message2: this is the message2 of father
```

- 修改 `my-config-test-config` 的 application.yml, 增加两项:

```
spring.cloud.config.parent-config.enabled: true
spring.cloud.config.parent-config.application: my-config-test-common
```

最终 `my-config-test-config` 的 application.yml 变为:

```
spring:
  application.name: 'my-config-test' 
  cloud.config: 
    password: '{cipher}AQB4hVdKfdq5m3/OUAot6cHsm0aBFnZ84MKBYoxplYKmyprJ0wmAHhjrYsytm1ItDR3Gtem6FLeqhkipRKPg2J+2dkmvcSWNi2qWz9dZ/fdPDnAdtI8g+mVwbrBn0y1wrwQyGMFlrW93biZJlNInSDtBJSX0FshPcv/p4E/p9RCw8IbuizI7d8O+Tr4CP2w21EUiQPDUQRB8BY0k3vqCULzOLvRTqnibgCcPsTk8+pZdYYNtCjuSbxcfrcogq2c1rrTKwbfWF4FjluKLTLfiobYNIkhASmagKq71LxpumJ5PwHR5FC1sEmv/mZsnNy09h36JYwF5zGbjog+Yu8wbVjosCsQWWg1gOliV8kkJ0BujELG956EtrTV+bm7m7AYzThA='
    parent-config: 
      enabled: true
      application: my-config-test-common

message: hello, home1-oss configserver!
```

- 现在访问 http://my-config-test:my-config-test@localhost:8888/config/my-config-test/development.env 时, 会发现已经多了一条"message2: this is the message2 of father", 而父项目message也在, 不过在装配成 yml 或 properties 时会被子 message 覆盖.
- 此时, 向客户端发送刷新配置请求 `curl -X POST http://localhost:8080/refresh` 客户端将会获得最新配置信息.  原因是我们再Controller上面加了`@RefreshScope`注解, 并在pom里面引入了`spring-boot-starter-actuator`. 这能使客户端在不重启的情况下, 实现配置动态刷新.

### 主动通知

`Spring cloud config` 默认通过 `Spring cloud bus`实现了修改配置以后的主动通知. 工作步骤如下:

1. 修改配置项目中的配置文件, 并`push`到 gitlab 
2. gitlab 配置项目中的 webhook 监听到`push`发生以后, 会`POST`一个请求到 configserver 服务端.
3. configserver 服务端接收到请求以后, 会通过 `Spring cloud bus`(MQ) 向队列中发送一个消息. 所有监听这个队列的客户端都会收到这个消息, 并主动去拉取最新配置并刷新. 实现配置推送到 git 仓库以后, 所有客户端都可以主动更新.

我们实现了两种推送规则: 

- `gitlab`上配置项目名字以`home1-oss-common`开头, 并且 `-config` 结尾内容发生变更, 将消息通知到 config-server 后, configer server 会通过 cloud bus 推送所有, 要求所有项目服务主动更新配置.
- 以`-config`结尾的项目, 会推送给相同前缀项目相关服务, 前缀是指: 第一个连接线`-`前面的字符. 例如: `xxx-config`, 会推送给 applicationName 以 `xxx` 开头的所有项目相关服务. 所以在取项目名字时要注意这一点.

> 现在支持 gitlab & gogs 两种 git 仓库的 web hook.

#### 主动通知配置示例

因客户端已经加了`spring-cloud-starter-bus-amqp`依赖, 服务端也已经加了`spring-cloud-starter-stream-rabbit`依赖. 并且已经配置好. 所以客户端和服务端之间的 `Spring cloud bus` 连接已经建好. 我们下面只需要配置 `gitlab` 的 webhook, 让发生变动时, gitlab 主动通知 config server即可.

- 配置 gitlab 的 webhook
 
在 gitlab 项目中 `setting` - `integration` 菜单下 URL 中输入 `http://xxx.xxx.xxx.xxx:8888/config/monitor` 并点击 添加按钮. (xxx.xxx.xxx.xxx 是机器IP, 不能用localhost)
> 注意: 
> 1. 如果用的是 docker 中的 gitlab, 在添加 webhook 时, 一定要用你电脑的IP, 因为 localhost 代表的是 gitlab 所在 docker 内部. 
> 2. 节点是 `/config/monitor`, 因为我们在所有访问 config server 相关请求前面都加了 /config 这一层.

- 添加完 webhook 以后, 可以点击`Test`, 看下是否报错(电脑慢的时候, gitlab 会给一个500, 这个看下日志正常即可). 
- 把`my-config-test-config` clone 到本地, 修改其中 message, 提交并 push 到 gitlab. 稍等片刻, 刷新 `http://localhost:8080/message`, 发现结果已经主动变成修改以后的了.

> spring cloud bus 客户端 Camden.SR5 版本有个 bug, 会在 MQ 中创建 `SpringCloudBusInput` 和 `SpringcloudBusOutput` 两个队列, 会把消息推到 output 里面, 而只监听 input, 导致无法主动刷新. 尽量不要使用.  
> 参考链接: https://github.com/spring-cloud/spring-cloud-bus/issues/55

### 其他

#### euraka 相关

前面我们都直接通过 IP 调用. 其实完全可以通过服务发现方式. 这里就不再赘述.


#### docker 相关

docker 脚本都已经准备好. 通过 `docker-compose up`命令, 可以直接打包docker运行.


#### 已知 bug

启动以后会发现日志里面会出现下面所示错误. 这是一个 Spring 的 bug. 不过不影响正常使用.

> https://github.com/spring-cloud/spring-cloud-netflix/issues/1055


```
2017-08-22T18:01:38,394 [1;31mERROR[m [32mc.n.d.TimedSupervisorTask [AsyncResolver-bootstrap-0] task supervisor rejected the task[m
java.util.concurrent.RejectedExecutionException: Task java.util.concurrent.FutureTask@76f0322b rejected from java.util.concurrent.ThreadPoolExecutor@7837d285[Terminated, pool size = 0, active threads = 0, queued tasks = 0, completed tasks = 0]
	at java.util.concurrent.ThreadPoolExecutor$AbortPolicy.rejectedExecution(ThreadPoolExecutor.java:2047) ~[?:1.8.0_101]
	at java.util.concurrent.ThreadPoolExecutor.reject(ThreadPoolExecutor.java:823) ~[?:1.8.0_101]
	at java.util.concurrent.ThreadPoolExecutor.execute(ThreadPoolExecutor.java:1369) ~[?:1.8.0_101]
	at java.util.concurrent.AbstractExecutorService.submit(AbstractExecutorService.java:112) ~[?:1.8.0_101]
	at com.netflix.discovery.TimedSupervisorTask.run(TimedSupervisorTask.java:62) [eureka-client-1.4.11.jar:1.4.11]
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511) [?:1.8.0_101]
	at java.util.concurrent.FutureTask.run(FutureTask.java:266) [?:1.8.0_101]
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$201(ScheduledThreadPoolExecutor.java:180) [?:1.8.0_101]
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:293) [?:1.8.0_101]
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) [?:1.8.0_101]
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617) [?:1.8.0_101]
	at java.lang.Thread.run(Thread.java:745) [?:1.8.0_101]
```