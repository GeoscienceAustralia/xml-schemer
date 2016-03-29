#!/usr/bin/env bash

JAR=`dirname $0`/../lib/xml-schemer-jar-with-dependencies.jar

if [ -n "${http_proxy}" ]; then
    HOST_AND_PORT=$(echo ${http_proxy} | cut -f 3 -d\/)
    PROXY_HOST=$(echo ${HOST_AND_PORT} | cut -f 1 -d:)
    PROXY_PORT=$(echo ${HOST_AND_PORT} | cut -f 2 -d:)
    PROXY_FOR_JAVA="-Dhttp.proxyHost=${PROXY_HOST} -Dhttp.proxyPort=${PROXY_PORT}"
fi

${JAVA_HOME}/bin/java ${PROXY_FOR_JAVA} -jar ${JAR} $@

