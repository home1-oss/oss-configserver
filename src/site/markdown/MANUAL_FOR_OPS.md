
# oss-configserver 运维手册

## 搭建生产环境

依赖服务:

+ git服务
    > - 本文以gitlab为例
    > - configserver管理的配置repository需建在同一个GROUP或用户账户下
    > - 默认为`configserver`
    > - 将git服务主机写到configserver的启动参数/环境变量中, [见样例](#PRODUCTION_EXAMPLE)

+ oss-eureka:latest
    > - 使用oss-eureka/docker-compose-production-peer.yml
    > - 将eureka集群信息写到configserver的启动参数/环境变量中, [见样例](#PRODUCTION_EXAMPLE)

+ mysql:5.6.31
    > - 使用oss-configserver/docker-compose-mysql.yml
    > - 为生产环境生成用户名/密码
    > - 将用户名/密码写到configserver的启动参数/环境变量中, [见样例](#PRODUCTION_EXAMPLE)

+ rabbitmq:3.6.5
    > - 使用oss-cloudbus/docker-compose.yml
    > - 为生产环境生成用户名/密码
    > - 将用户名/密码写到通用配置中
    > - 将用户名/密码写到configserver的启动参数/环境变量中, [见样例](#PRODUCTION_EXAMPLE)

配置checklist:

+ 确定git服务地址
    > - 需保证configserver可以访问git服务, 包括http(s)和ssh端口
    > - 需保证git服务可以访问configserver的http(s)端口, 默认为8888, 稍后需要在configserver上配置白名单.

+ 为生成环境的通用配置创建git repository
    > - 生产环境的通用配置repository名称默认为`common-production-config`
    > - 生产环境的通用配置repository分支默认为`production.env`

+ git服务上每个配置repository中配置webhook
    > - 以gitlab为例
    > - 只启用Push events, 不开启SSL verification
    > - 地址为http://${configserver-host}:8888/monitor
    > - 不使用Secret Token, 通过在configserver上配置白名单来允许git服务访问.

+ 为生产环境生成deploy key
    > - 生成密钥
    > - 将公钥设置到git服务上每个配置repository中
    > - 将私钥放在configserver的docker容器可以mount的位置

+ 设置configserver使用的deploy key, [见样例](#PRODUCTION_EXAMPLE)
+ 设置configserver的monitor白名单, [见样例](#PRODUCTION_EXAMPLE)
+ 设置configserver的eureka地址, [见样例](#PRODUCTION_EXAMPLE)
+ 设置configserver的mysql用户名/密码, [见样例](#PRODUCTION_EXAMPLE)
+ 设置configserver的rabbitmq的用户名/密码, [见样例](#PRODUCTION_EXAMPLE)
+ 生成configserver管理员用户名/密码, [见样例](#PRODUCTION_EXAMPLE)
+ 设置configserver使用的keystore
    > - [为生产环境生成keystore](#CREATE_KEYSTORE)
    > - 配置[见样例](#PRODUCTION_EXAMPLE)

+ 设置SPRING_CLOUD_CONFIG_SERVER_MONITOR_WHITELIST
    > - 配置[见样例](#PRODUCTION_EXAMPLE)

+ [权限管理](./PERMISSIONS.html)

## <a name="NON_PRODUCTION_EXAMPLE">搭建测试环境 example</a>

    # see: http://stackoverflow.com/questions/13322485/how-to-i-get-the-primary-ip-address-of-the-local-machine-on-linux-and-os-x

    export GIT_HOST=gitlab.internal
    export SPRING_CLOUD_CONFIG_SERVER_DEPLOYKEY="classpath:default_deploy_key"
    export SPRING_CLOUD_CONFIG_SERVER_MONITOR_WHITELIST="${GIT_HOST},$(dig +short ${GIT_HOST})"
    
    export EUREKA_CLIENT_SERVICEURL_DEFAULTZONE="http://user:user_pass@oss-eureka-peer1.internal:8761/eureka/,http://user:user_pass@oss-eureka-peer2.internal:8761/eureka/,http://user:user_pass@oss-eureka-peer3.internal:8761/eureka/"
    export EUREKA_INSTANCE_HOSTNAME="oss-configserver.internal"
    
    export MYSQL_ROOT_PASSWORD="$(echo `</dev/urandom tr -dc A-Za-z0-9 | head -c16`)"
    # assume db on same host
    export DB_ADDR="$(ip route get 1 | awk '{print $NF;exit}')"
    export DB_USER="user"
    export DB_PASS="user_pass"
    
    # assume rabbitmq on same host
    export SPRING_RABBITMQ_HOST="$(ip route get 1 | awk '{print $NF;exit}')"
    export SPRING_RABBITMQ_USERNAME="user"
    export SPRING_RABBITMQ_PASSWORD="user_pass"
    
    export SECURITY_USER_PASSWORD="admin_pass"
    
    export ENCRYPT_KEYSTORE_LOCATION="classpath:keystore.jks"

## <a name="PRODUCTION_EXAMPLE">搭建生产环境 example</a>

    export GIT_HOST=gitlab.internal
    ssh-keygen -t rsa -b 2048 -f production-deploy_key -q -N "" -C "production-configserver@home1.cn"
    export SPRING_CLOUD_CONFIG_SERVER_DEPLOYKEY="file:/root/production-deploy_key"
    echo "SAVE_THIS deploy_key: $(pwd)/production-deploy_key"
    export SPRING_CLOUD_CONFIG_SERVER_MONITOR_WHITELIST="${GIT_HOST},$(dig +short ${GIT_HOST})"
    
    export EUREKA_CLIENT_SERVICEURL_DEFAULTZONE="http://user:user_pass@oss-eureka-peer1.internal:8761/eureka/,http://user:user_pass@oss-eureka-peer2.internal:8761/eureka/,http://user:user_pass@oss-eureka-peer3.internal:8761/eureka/"
    export EUREKA_INSTANCE_HOSTNAME="oss-configserver.internal"
    
    export MYSQL_ROOT_PASSWORD="$(echo `</dev/urandom tr -dc A-Za-z0-9 | head -c16`)"
    # assume db on same host
    export DB_ADDR="$(ip route get 1 | awk '{print $NF;exit}')"
    export DB_USER="user"
    export DB_PASS="$(echo `</dev/urandom tr -dc A-Za-z0-9 | head -c16`)"
    echo "SAVE_THIS MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}"
    echo "SAVE_THIS DB_PASS: ${DB_PASS}"
    
    # assume rabbitmq on same host
    export SPRING_RABBITMQ_HOST="$(ip route get 1 | awk '{print $NF;exit}')"
    export SPRING_RABBITMQ_USERNAME="user"
    export SPRING_RABBITMQ_PASSWORD="$(echo `</dev/urandom tr -dc A-Za-z0-9 | head -c16`)"
    echo "SAVE_THIS SPRING_RABBITMQ_PASSWORD: ${SPRING_RABBITMQ_PASSWORD}"
    
    export SECURITY_USER_PASSWORD="$(echo `</dev/urandom tr -dc A-Za-z0-9 | head -c8`)"
    echo "SAVE_THIS SECURITY_USER_PASSWORD: ${SECURITY_USER_PASSWORD}"
    
 keyStore配置见: [为生产环境生成keystore](#CREATE_KEYSTORE)

## Build-in deploy key for non-production environments

    ssh-keygen -t rsa -b 2048 -f src/main/resources/default_deploy_key -q -N "" -C "configserver@home1.cn"

## Cipher and keystore

1. 使用对称加密
    > - 优点：设置简单如下
    > - encrypt.key: changeme # 对称加密秘钥

2. 使用非对称加密，如使用keyStore:

  ***前置条件***：需要从oracle官网下载(JCE)Unlimited Strength Jurisdiction Policy Files(两个jar)放入jre的lib/security

  configserver的bootstrap.yml配置:

    encrypt:                              # 配置项加密配置
      keyStore:
        alias: key_alias                  # keyPair别名
        location: classpath:/keystore.jks # keyStore位置
        password: store_pass              # keyStore密码
        secret: key_pass                  # keyPair密码

## Build-in keystore for non-production environments

    KEYSTORE_KEY_C="CN"
    KEYSTORE_KEY_CN="home1"
    KEYSTORE_KEY_L="Beijing"
    KEYSTORE_KEY_O="home1"
    KEYSTORE_KEY_OU="home1"
    KEYSTORE_KEY_S="Beijing"
    ENCRYPT_KEYSTORE_ALIAS="key_alias"
    ENCRYPT_KEYSTORE_LOCATION="keystore.jks"
    ENCRYPT_KEYSTORE_PASSWORD="store_pass"
    ENCRYPT_KEYSTORE_SECRET="key_pass"
    
    keytool -genkeypair -alias ${ENCRYPT_KEYSTORE_ALIAS} -keyalg RSA -keysize 2048 -validity 3650 -dname "CN=${KEYSTORE_KEY_CN},OU=${KEYSTORE_KEY_OU},O=${KEYSTORE_KEY_O},L=${KEYSTORE_KEY_L},S=${KEYSTORE_KEY_S},C=${KEYSTORE_KEY_C}" -keypass ${ENCRYPT_KEYSTORE_SECRET} -storepass ${ENCRYPT_KEYSTORE_PASSWORD} -keystore ${ENCRYPT_KEYSTORE_LOCATION}
    
    echo "encrypt.keyStore.alias: ${ENCRYPT_KEYSTORE_ALIAS}"
    echo "encrypt.keyStore.location: ${ENCRYPT_KEYSTORE_LOCATION}"
    echo "encrypt.keyStore.password: ${ENCRYPT_KEYSTORE_PASSWORD}"
    echo "encrypt.keyStore.secret: ${ENCRYPT_KEYSTORE_SECRET}"

  Can change alias and keypass then run again to generate multiple keypair.  
  Use `keytool -list -keystore keystore.jks` to list keypairs.  
  Note: Create separate keystore for production environment

## <a name="CREATE_KEYSTORE">Create keystore for production environment</a>

  Random password is more secure.

    KEYSTORE_KEY_C="CN"
    KEYSTORE_KEY_CN="home1"
    KEYSTORE_KEY_L="Beijing"
    KEYSTORE_KEY_O="home1"
    KEYSTORE_KEY_OU="home1"
    KEYSTORE_KEY_S="Beijing"
    ENCRYPT_KEYSTORE_ALIAS="production-key"
    ENCRYPT_KEYSTORE_LOCATION="production-keystore.jks"
    ENCRYPT_KEYSTORE_PASSWORD="$(openssl rand -base64 32)"
    ENCRYPT_KEYSTORE_SECRET="$(echo `</dev/urandom tr -dc A-Za-z0-9 | head -c16`)"
    
    keytool -genkeypair -alias ${ENCRYPT_KEYSTORE_ALIAS} -keyalg RSA -keysize 2048 -validity 3650 -dname "CN=${KEYSTORE_KEY_CN},OU=${KEYSTORE_KEY_OU},O=${KEYSTORE_KEY_O},L=${KEYSTORE_KEY_L},S=${KEYSTORE_KEY_S},C=${KEYSTORE_KEY_C}" -keypass ${ENCRYPT_KEYSTORE_SECRET} -storepass ${ENCRYPT_KEYSTORE_PASSWORD} -keystore ${ENCRYPT_KEYSTORE_LOCATION}
    
    echo "encrypt.keyStore.alias: ${ENCRYPT_KEYSTORE_ALIAS}"
    echo "encrypt.keyStore.location: ${ENCRYPT_KEYSTORE_LOCATION}"
    echo "encrypt.keyStore.password: ${ENCRYPT_KEYSTORE_PASSWORD}"
    echo "encrypt.keyStore.secret: ${ENCRYPT_KEYSTORE_SECRET}"
    
    export ENCRYPT_KEYSTORE_ALIAS=${ENCRYPT_KEYSTORE_ALIAS}
    export ENCRYPT_KEYSTORE_LOCATION="file:/root/${ENCRYPT_KEYSTORE_LOCATION}"
    export ENCRYPT_KEYSTORE_PASSWORD=${ENCRYPT_KEYSTORE_PASSWORD}
    export ENCRYPT_KEYSTORE_SECRET=${ENCRYPT_KEYSTORE_SECRET}
