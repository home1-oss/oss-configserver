
# oss-configserver 管理员手册

## 处理新建application的申请

+ 确保应用名称未被使用
    > - 应用名称为应用的`${spring.application.name}`
    > - 应用应具备相应的域名
    > - 开发/测试环境为 `${spring.application.name}.internal`
    > - 生成环境为 `${spring.application.name}.idc`
+ 为应用生成一个随机密码
    > - 方法为 TODO
+ 为应用创建用户
    > - 方法为 TODO
+ 在git服务上为应用创建配置repository
    > - 名称为: `${spring.application.name}-config`
    > - 内容为: TODO
+ 为配置repository设置webhook
    > - 需设置开发/测试环境及生产环境的webhook
+ 为配置repository设置deploy key
    > - 需设置开发/测试环境及生产环境的deploy key

#### Decrypt

  Need admin permission.

    SECURITY_USER_PASSWORD="admin_pass";
    
    curl -i -u admin:${SECURITY_USER_PASSWORD} -X POST "http://local-configserver:8888/config/decrypt" -d '{cipher}AQAcBZjNSNIT4dFJR0mzqzVOVY2OsKim3UQyei7TXZ+VCaBVHKEX2ztFwAMaZr7LABZYAkJG/3+tfnrQoA4NsQGH0YybIMui55cyQCbMtaItRlzy9uegnRwJ5w4XOqJVdglthpqNldeKt2dxXj/C1UnHijvNWjZ+BnDc7b9mTgt4pi7dLHfaLD3tuddvRDrYiaR4oNDFn7qkEz52Jk3ooYhomr+O5QH6VTqQcVqmOJF54XPiFCFoMho9m115BHaLvqL02g26hirFuDd2+JqFXo6mxFpRHZeOKeqUKQFdIDYQarmiLp21RL4lYpao2ePtA4CKqDOwntC4zXtKHmA8NOosxtxRUAZ1Sdp9CPjur5Ws/A7+uSUC6TwLqCRGxTLq8dY='
    # or
    curl -i -u admin:${SECURITY_USER_PASSWORD} -X POST "http://local-configserver:8888/config/decrypt" -d 'AQAcBZjNSNIT4dFJR0mzqzVOVY2OsKim3UQyei7TXZ+VCaBVHKEX2ztFwAMaZr7LABZYAkJG/3+tfnrQoA4NsQGH0YybIMui55cyQCbMtaItRlzy9uegnRwJ5w4XOqJVdglthpqNldeKt2dxXj/C1UnHijvNWjZ+BnDc7b9mTgt4pi7dLHfaLD3tuddvRDrYiaR4oNDFn7qkEz52Jk3ooYhomr+O5QH6VTqQcVqmOJF54XPiFCFoMho9m115BHaLvqL02g26hirFuDd2+JqFXo6mxFpRHZeOKeqUKQFdIDYQarmiLp21RL4lYpao2ePtA4CKqDOwntC4zXtKHmA8NOosxtxRUAZ1Sdp9CPjur5Ws/A7+uSUC6TwLqCRGxTLq8dY='
    
  Got 'mysecret'

## Security (multiple application authentication)

>configserver增加了对项目访问权限的访问控制，每个工程只能访问自己的工程配置。config-server提供一个管理员角色管理角色权限相关
 具体见[MANUAL_FOR_ADMIN](./MANUAL_FOR_ADMIN.html)


## User management

  Only admin can manage users.

#### Create user

    SECURITY_USER_PASSWORD="admin_pass";
    APP_NAME="oss-todomvc-thymeleaf";
    APP_PASS="user_pass";
    
    curl -i -u admin:${SECURITY_USER_PASSWORD} -X POST -H 'Content-Type: application/x-www-form-urlencoded;' \
        -d "password=${APP_PASS}" "http://local-configserver:8888/config/users/${APP_NAME}/"

#### Delete user

    curl -i -u admin:${SECURITY_USER_PASSWORD} -X DELETE "http://local-configserver:8888/config/users/${APP_NAME}/"

#### Get user

    curl -i -u admin:${SECURITY_USER_PASSWORD} -X GET "http://local-configserver:8888/config/users/${APP_NAME}/"

#### Update user's password

    curl -i -u admin:${SECURITY_USER_PASSWORD} -X PUT -H 'Content-Type: application/x-www-form-urlencoded;' \
        -d "password=${APP_PASS}" "http://local-configserver:8888/config/users/${APP_NAME}/"





## Generate a keypair for accessing git repository

    ssh-keygen -t rsa -b 2048 -f src/main/resources/deploy_key -q -N "" -C "configserver@home1.cn"

  Use src/main/resources/deploy_key.pub as deploy key in config projects.
  Default one is 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDJexpGshox4d2mRhYIjOjxlAmcF9k9fKzlr2ylKS32LwMrVeKY+XyV06YvX0FE0uwj3DSp2Vai2e8kEylRDhQmuV1ZjjA08P9/j9SacFuzY8TfncdUwsQ3wxmBjmlpQoODUad7v0ld0r1AfttqbfGJr8L5gPzxvoA96K+6PkYyzUwbStJiW0ruNEVOb5LgN/v90LWMorwXj2Y/fu+i5OWp+iCTrQ6ltC6xQ/f3MyRMbfUxW3cXNp9UkdVkFDJ4Le/5poim5yPi6d2vjG8z7h5hM7M+H7q72hVoH9Rx0yzp55jOSRMXDGU138pK6HQFU/mCw9yaT0OwGK5IdvaX+ryd configserver@home1.cn'

## Run git service or use public git service

#### Create a group for config projects (optional)

#### Push a common-config project to git service

  private project with a application.yml at it's root directory.

    # in configserver's application.yml
    spring.cloud.config.server.common-config.application: application

#### Add ssh public key to common-config project on git service as a deploy key

    git config --global http.sslVerify false

## Build package

    mvn clean package;

## Run

TODO fixme

    SECURITY_USER_PASSWORD="admin_pass";
    DB_ADDR="local-mysql";
    DB_PORT="3306";
    DB_USER="configserver";
    DB_PASS="configserver";
    ENCRYPT_KEYSTORE_SECRET="key_pass";
    ENCRYPT_KEYSTORE_PASSWORD="store_pass";
    GIT_PREFIX="http://gitlab.internal/configserver";
