#!/bin/sh

export JAVA_HOME=$JAVA_HOME
export JAVA_VM="-Xmx1024m -Xms256m -XX:PermSize=256M"
#指定字符集  
export LANG=zh_CN.UTF-8
RUN_HOME=.
CLASSPATH=$CLASSPATH:$RUN_HOME/lib/ojdbc14.jar
CLASSPATH=$CLASSPATH:$RUN_HOME/lib/log4j-1.2.8.jar
CLASSPATH=$CLASSPATH:$RUN_HOME/lib/mysql-connector-java-5.1.44.jar
CLASSPATH=$CLASSPATH:$RUN_HOME/lib/ESBDataSimulate.jar
export CLASSPATH

echo "JAVA_HOME="$JAVA_HOME
echo "JAVA_VM="$JAVA_VM
echo "CLASSPATH="$CLASSPATH

$JAVA_HOME/jre/bin/java  com.dc.esb.DataSimulate &
echo "启动完成!"
