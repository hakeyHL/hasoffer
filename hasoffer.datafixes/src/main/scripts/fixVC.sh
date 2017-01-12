#!/bin/bash

BIN_DIR=`pwd`
echo "BIN_DIR: $BIN_DIR"
cd ..
DEPLOY_DIR=`pwd`
USER=hasoffer
GROUP=hasoffer
SERVER_NAME=""
SERVER_PROTOCOL=""
SERVER_PORT=""
LOGS_FILE=""
if [ -z "$SERVER_NAME" ]; then
    SERVER_NAME=`hostname`
fi

LOGS_DIR="$DEPLOY_DIR/logs/"
if [ ! -d $LOGS_DIR ]; then
    mkdir -p $LOGS_DIR
    chown -R $USER.$GROUP $LOGS_DIR
fi
#STDOUT_FILE=$LOGS_DIR/`basename $DEPLOY_DIR`.log
LIB_DIR=$DEPLOY_DIR/lib
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`
JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "
JAVA_DEBUG_OPTS=""
if [ "$1" = "debug" ]; then
    JAVA_DEBUG_OPTS=" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n "
fi
JAVA_JMX_OPTS=""
if [ "$1" = "jmx" ]; then
    JAVA_JMX_OPTS=" -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false "
fi
JAVA_MEM_OPTS=""
BITS=`java -version 2>&1 | grep -i 64-bit`
if [ -n "$BITS" ]; then
    JAVA_MEM_OPTS=" -server -Xmx256m -Xms128m -Xmn128m -Xss256k"
else
    JAVA_MEM_OPTS=" -server -Xms256m -Xmx128m -XX:SurvivorRatio=2 -XX:+UseParallelGC "
fi
echo -e "Starting the $SERVER_NAME ...\c"
java $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPTS $JAVA_JMX_OPTS -classpath $CONF_DIR:$LIB_JARS hasoffer.datafixes.order.Main $2 $3 $4
echo "Finish and Success."