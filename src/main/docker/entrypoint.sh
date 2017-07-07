#!/usr/bin/env bash





if [ -z "${GIT_HOST}" ]; then echo "GIT_HOST not defined."; exit 1; fi
# ping ${GIT_HOST} -c 1
if [ ! -z "${GIT_HOST}" ]; then echo "wait ${GIT_HOST}:22 for 30 seconds."; /usr/bin/waitforit -full-connection=tcp://${GIT_HOST}:22 -timeout=30; fi
# rabbit mq is password protected, so try http management port
if [ ! -z "${SPRING_RABBITMQ_HOST}" ] && [ ! -z "${SPRING_RABBITMQ_PORT}" ]; then echo "wait ${SPRING_RABBITMQ_HOST}:1${SPRING_RABBITMQ_PORT} for 30 seconds."; /usr/bin/waitforit -full-connection=tcp://${SPRING_RABBITMQ_HOST}:1${SPRING_RABBITMQ_PORT} -timeout=30; fi

JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:/dev/urandom";
JAVA_OPTS="${JAVA_OPTS} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}";

# if command starts with an option, prepend java
if [ "${1:0:1}" == '-' ]; then
    set -- java "$@" -jar *-exec.jar
elif [ "${1:0:1}" != '/' ]; then
    set -- java ${JAVA_OPTS} -jar *-exec.jar "$@"
fi

exec "$@"

## arguments: target
## returns: git_deploy_key_file
#git_deploy_key_file() {
#    local archive=$(ls | grep .jar | grep -v sources.jar | grep -v jar.original)
#    local target=$1
#    local result=""
#    if [ -z "${SPRING_CLOUD_CONFIG_SERVER_DEPLOYKEY}" ]; then
#        extract_file_from_archive ${archive} BOOT-INF/classes/deploy_key.pub ${target}
#        if [ -f ${target} ]; then
#            result=${target}
#        else
#            result=""
#        fi
#    elif [ ${SPRING_CLOUD_CONFIG_SERVER_DEPLOYKEY:0:10} == classpath: ]; then
#        extract_file_from_archive ${archive} BOOT-INF/classes/${SPRING_CLOUD_CONFIG_SERVER_DEPLOYKEY:10}.pub ${target}
#        if [ -f ${target} ]; then
#            result=${target}
#        else
#            result=""
#        fi
#    elif [ ${SPRING_CLOUD_CONFIG_SERVER_DEPLOYKEY:0:5} == file: ] && [ -f ${SPRING_CLOUD_CONFIG_SERVER_DEPLOYKEY:0:5} ]; then
#        result=${SPRING_CLOUD_CONFIG_SERVER_DEPLOYKEY:5}
#    elif [ -f ${SPRING_CLOUD_CONFIG_SERVER_DEPLOYKEY} ]; then
#        result=${SPRING_CLOUD_CONFIG_SERVER_DEPLOYKEY}
#    else
#        result="";
#    fi
#    if [ -f ${result} ]; then
#        chmod 600 ${result}
#    fi
#    echo ${result}
#}
