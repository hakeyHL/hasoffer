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
PIDS=`ps -f | grep java | grep "$BIN_DIR" |awk '{print $2}'`
if [ -n "$PIDS" ]; then
    echo "ERROR: The $SERVER_NAME already started!"
    echo "PID: $PIDS"
    exit 1
fi
if [ -n "$SERVER_PORT" ]; then
    SERVER_PORT_COUNT=`netstat -tln | grep $SERVER_PORT | wc -l`
    if [ $SERVER_PORT_COUNT -gt 0 ]; then
        echo "ERROR: The $SERVER_NAME port $SERVER_PORT already used!"
        exit 1
    fi
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
nohup java $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPTS $JAVA_JMX_OPTS -classpath $CONF_DIR:$LIB_JARS hasoffer.datafixes.order.Main  >/dev/null 2>&1 &
COUNT=0
while [ $COUNT -lt 1 ]; do
    echo -e ".\c"
    sleep 1
    if [ -n "$SERVER_PORT" ]; then
        if [ "$SERVER_PROTOCOL" == "dubbo" ]; then
            COUNT=`echo status | nc -i 1 127.0.0.1 $SERVER_PORT | grep -c OK`
        else
            COUNT=`netstat -an | grep $SERVER_PORT | wc -l`
        fi
    else
        COUNT=`ps -f | grep java | grep "$DEPLOY_DIR" | awk '{print $2}' | wc -l`
    fi
    if [ $COUNT -gt 0 ]; then
        break
    fi
done
echo "OK!"
PIDS=`ps -f | grep java | grep "$DEPLOY_DIR" | awk '{print $2}'`
echo "PID: $PIDS"
#echo "STDOUT: $STDOUT_FILE"
echo "Start Success."