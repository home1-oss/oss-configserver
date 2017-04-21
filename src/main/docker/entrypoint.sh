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
