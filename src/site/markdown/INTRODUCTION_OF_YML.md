
## yml与properties的对比与转换

键-值:
```
server:
  a:
    url: http://dev.bar.com
    name: Developer Setup
  b:
    url: http://foo.bar.com
    name: My Cool App       
```

上面的YAML与下面的properties等效: 
    
```
server.a.url=http://dev.bar.com
server.a.name=Developer Setup
server.b.url=http://foo.bar.com
server.b.name=My Cool App
```

列表:
YAML列表被表示成使用[index]间接引用作为属性keys的形式, 例如: 

```
my: 
  servers:
    -  dev.bar.com
    -  foo.bar.com
```

上面的YAML与下面的properties等效: 
```
my.servers[0]=dev.bar.com
my.servers[1]=foo.bar.com
```

使用Spring DataBinder工具绑定列表属性 (这是@ConfigurationProperties做的事), 
你需要确定目标bean中有个java.util.List或Set类型的属性, 并且需要提供一个setter或使用可变的值初始化它.

比如, 下面的代码将绑定上面的属性:

    @ConfigurationProperties(prefix="my")
    public class Config {
    
      private List<String> servers = new ArrayList<>();
      
      public List<String> getServers() {
        return this.servers;
      }
    }


## multi-profile yml配置
你可以在单个文件中定义多个特定配置（profile-specific）的YAML文档，并通过一个spring.profiles key标示应用的文档。例如：

```
server:
  address: 192.168.1.100

---  # ---用来分隔 profile
spring:
  profiles: development.env   #开发环境

server:
  address: 127.0.0.1

---
spring:
  profiles: production.env    #生产环境

server:
  address: 192.168.1.120
```

在上面的例子中, 如果development.env配置(profile)被激活, 那server.address属性将是127.0.0.1.
如果development.env或production.env配置(profile)没有启用, 则该属性的值将是192.168.1.100.  

## spring-boot的yml配置

>只要SnakeYAML库在classpath下, SpringApplication类就会自动支持YAML配置并提供yml到properties的转换.  

>Spring框架提供两个便利的类用于加载YAML文档, YamlPropertiesFactoryBean会将YAML作为properties来加载, YamlMapFactoryBean会将YAML作为Map来加载.  

## 参考文档

[in-yaml-how-do-i-break-a-string-over-multiple-lines](http://stackoverflow.com/questions/3790454/in-yaml-how-do-i-break-a-string-over-multiple-lines)
