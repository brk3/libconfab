#!/bin/sh

java \
    -ea \
    -classpath .:/usr/lib/jvm/java-6-openjdk/jre/lib:../lib/httpcomponents-client-4.1.1/lib/*:../lib/jsoup-1.4.1.jar \
    -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog \
    -Dorg.apache.commons.logging.simplelog.showdatetime=true \
    -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG \
    ProtoType

