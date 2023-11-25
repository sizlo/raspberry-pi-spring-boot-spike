#!/bin/bash

log () {
  DATE=$(date "+%Y-%m-%d_%H:%M:%S.%N")
  echo "$DATE - $@"
}

FOLDER=$HOME/raspberry-pi-spring-boot-spike

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
  if ! which jq &> /dev/null
  then
      log "Installing jq"
      sudo apt install jq -y
      log "jq installed"
  fi

  log "Deleting previous jars"
  rm -rf $FOLDER/*.jar
  log "Previous jars deleted"

  log "Downloading latest jar"
  RELEASE_JSON="$(curl https://api.github.com/repos/sizlo/raspberry-pi-spring-boot-spike/releases/latest)"
  JAR_FILENAME="$(echo $RELEASE_JSON | jq -r '.assets[0].name')"
  URL="$(echo $RELEASE_JSON | jq -r '.assets[0].browser_download_url')"
  wget -P $FOLDER $URL
  log "jar downloaded"

  log "Removing stale deployment logs"
  ls $FOLDER/deploy_*.log | sort -r | tail +6 | xargs -I {} rm {}
  log "Removed stale deployment logs"

  log "Sourcing app environment from file"
  source $FOLDER/raspberrypi.env
  log "Sourced app environment"

  log "Deployment complete, running jar"
} > $FOLDER/deploy_$(date "+%Y-%m-%d_%H:%M:%S.%N").log 2>&1

$JAVA_COMMAND -jar $FOLDER/$JAR_FILENAME
