#!/usr/bin/env bash

JAR=`dirname $0`/../lib/xml-schemer-jar-with-dependencies.jar
java -jar ${JAR} $@

