
# Configserver压测报告

## 测试环境  

|环境 | 值 |     
|--- | --- |  
|CPU | ntel(R) Core(TM) i7-4790 CPU @ 3.60GHz 8 cores |  
|RAM |	8 GB，1600MHz |  
|硬盘IO |	1 TB，7200rpm |  
|网卡 |	1 Gigabit Ethernet |  
|操作系统 |	CentOS Linux release 7.2.1511 (Core) |  
| 网络ping值 | --- ping statistics --- <br/> 17 packets transmitted, 17 received, 0% packet loss, time 16000ms<br/>rtt min/avg/max/mdev = 0.123/0.167/0.204/0.024 ms|  

## 测试Configserver

*** 测试方案 ***  
第一步：用docker启动ConfigServer  
第二步：P（P>0）个链接同时发送HTTP请求  

*** 结果 ***  
| Requests per second | Time per request（ms)|  
| --- |--- |   
|  6738.68 | 0.148 [ms] |

*** 分析 ***  
通常服务启用的时候才会调用Configserver，所以Configserver的压力不会太大，6k/s的吞吐量足够满足其性能需求 

### 测试方法  
采用ApacheBench测试，参考指令如下：  
 ab -n 10000 -c 100 [http[s]://]hostname[:port]/path  
