#!/bin/bash

log () {
  DATE=$(date "+%Y-%m-%d_%H:%M:%S.%N")
  echo "$DATE - $@"
}

FOLDER=$HOME/raspberry-pi-spring-boot-spike

# jq is installed here, but when run from cron this directory is not on the path
PATH=$PATH:/usr/bin

{
  log "Deployment started"

  # Install java if required
  log "Checking for java 17 on the PATH"
  if java --version | grep -q "\b17\b"; then
    JAVA_COMMAND="java"
    log "Found java 17 on the PATH"
  else
    log "Could not find java 17 on the PATH"
    log "Checking for java 17 in app folder"
    if $FOLDER/jdk-17-ga/bin/java --version | grep -q "\b17\b"; then
      JAVA_COMMAND=$FOLDER/jdk-17-ga/bin/java
      log "Found java 17 in app folder"
    else
      log "Could not find java 17 in app folder"
      log "Installing raspberry pi 1 compatible jdk..."
      wget -P $FOLDER https://github.com/sizlo/raspberry-pi-spring-boot-spike/raw/main/deployment-resources/jdk-17-ga_gcc-10.1_glibc-2.28_binutils-2.31_Buster.tar.gz
      tar zxf $FOLDER/jdk-17-ga_gcc-10.1_glibc-2.28_binutils-2.31_Buster.tar.gz -C $FOLDER
      log "Raspberry pi 1 compatible jdk installed"
      JAVA_COMMAND=$FOLDER/jdk-17-ga/bin/java
    fi
  fi

  # Install jq if required
  log "Checking for jq on the PATH"
  if which jq &> /dev/null
  then
    log "Found jq on the PATH"
  else
    log "Installing jq"
    sudo apt install jq -y
    log "jq installed"
  fi

  log "Getting latest release info"
  RELEASE_JSON="$(curl https://api.github.com/repos/sizlo/raspberry-pi-spring-boot-spike/releases/latest)"
  JAR_FILENAME="$(echo $RELEASE_JSON | jq -r '.assets[0].name')"
  URL="$(echo $RELEASE_JSON | jq -r '.assets[0].browser_download_url')"
  RELEASE_JAR_SHA1SUM="$(echo $RELEASE_JSON | jq -r '.body' | grep '^jar sha1sum:' | cut -f 3 -d ' ')"
  log "Got latest release info"

  EXISTING_JAR_FILEPATH=$(ls $FOLDER/*.jar)
  EXISTING_JAR_SHA1SUM=$(sha1sum $EXISTING_JAR_FILEPATH | cut -f 1 -d " ")

  if [ $EXISTING_JAR_SHA1SUM = $RELEASE_JAR_SHA1SUM ]
  then
    log "Local jar sha1sum matches release jar sha1sum, skipping jar download"
    JAR_FILEPATH=$EXISTING_JAR_FILEPATH
  else
    log "Deleting previous jars"
    rm -rf $FOLDER/*.jar
    log "Previous jars deleted"
    log "Downloading jar from release"
    wget -P $FOLDER $URL
    log "jar downloaded"
    JAR_FILEPATH=$FOLDER/$JAR_FILENAME
  fi

  log "Removing stale deployment logs"
  ls $FOLDER/deploy_*.log | sort -r | tail +6 | xargs -I {} rm {}
  log "Removed stale deployment logs"

  log "Sourcing app environment from file"
  . $FOLDER/raspberrypi.env
  log "Sourced app environment"

  log "Deployment complete, running jar"
} > $FOLDER/deploy_$(date "+%Y-%m-%d_%H:%M:%S.%N").log 2>&1

$JAVA_COMMAND -jar $JAR_FILEPATH
