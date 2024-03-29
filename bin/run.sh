#!/bin/bash

get_java_cmd() {
  if [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo "$JAVA_HOME/bin/java"
  else
    echo "java"
  fi
}
declare -r java_cmd=$(get_java_cmd)
declare -r java_version=$("$java_cmd" -version 2>&1 | awk -F '"' '/version/ {print $2}')
if [[ "${java_version}" > "1.8" ]]; then
  memopts="-XX:MaxMetaspaceSize=384m"
else
  memopts="-XX:MaxPermSize=384m"
fi

# You may need to customize memory config below to optimize for your environment.
# To display time when the application is stopped for GC:
# -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationStoppedTime
JAVA_OPTS="-Xmx1024m -Xms256m $memopts -XX:+HeapDumpOnOutOfMemoryError -XX:+AggressiveOpts -XX:+OptimizeStringConcat -XX:+UseFastAccessorMethods -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+CMSClassUnloadingEnabled -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=1 -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly -Djava.awt.headless=true -server -Dxitrum.mode=production"

# Quote because path may contain spaces
if [ -h $0 ]
then
  ROOT_DIR="$(cd "$(dirname "$(readlink -n "$0")")" && pwd)"
else
  ROOT_DIR="$(cd "$(dirname $0)" && pwd)"
fi
cd "$ROOT_DIR"

# Include ROOT_DIR to do "ps aux | grep java" to find this pid easier when
# starting multiple processes from different directories
CLASS_PATH="$ROOT_DIR/lib/*:$ROOT_DIR/gui_lib/*:config"

# Use exec to be compatible with daemontools:
# http://cr.yp.to/daemontools.html
export easy_rest_mode=production
exec $java_cmd $JAVA_OPTS -cp $CLASS_PATH $@ net.juniper.ems.EmsBoot