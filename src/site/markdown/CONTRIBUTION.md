# configserver 开发文档

## 构建docker镜像

    DOCKER_HOST=unix:///var/run/docker.sock mvn -Dmaven.test.skip=true clean package docker:build docker:push
    
    MAVEN_OPTS="${MAVEN_OPTS} -Ddocker.registry=home1oss" mvn clean package

## docker镜像debug

    docker run --rm -it \
        -e GIT_HOST=gitlab.local \
        --network oss-network \
        --link gitlab.local:gitlab.local \
        -v /root/data/configserver \
        -v ${HOME}/ws/architecture/common-config:/root/workspace/common-config \
        -v ${HOME}/ws/architecture/oss-todomvc-app-config:/root/workspace/oss-todomvc-app-config \
        -v ${HOME}/ws/architecture/oss-todomvc-thymeleaf-config:/root/workspace/oss-todomvc-thymeleaf-config \
        registry.docker.local/oss-configserver \
        shell
