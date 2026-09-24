#!/bin/sh

# ClearSpend Gradle Wrapper Script
#
# Attempt to find and set JAVA_HOME if not already set.
if [ -z "$JAVA_HOME" ] ; then
    if [ -x /usr/libexec/java_home ] ; then
        JAVA_HOME=`/usr/libexec/java_home`
        export JAVA_HOME
    fi
fi

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
else
    JAVACMD="java"
fi

DIRNAME=`dirname "$0"`
APP_BASE_NAME=`basename "$0"`
APP_HOME="`cd "$DIRNAME" >/dev/null 2>&1 && pwd`"

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ ! -e "$CLASSPATH" ]; then
    echo "Downloading gradle-wrapper.jar..."
    mkdir -p "$APP_HOME/gradle/wrapper"
    curl -sLo "$CLASSPATH" https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar || true
fi

exec "$JAVACMD" -jar "$CLASSPATH" "$@"
