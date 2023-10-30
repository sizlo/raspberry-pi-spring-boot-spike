#!/bin/bash

# Install java if required
if [ ! -d "jdk-17-ga" ]; then
  echo "Installing raspberry pi 1 compatible jdk..."
  wget https://github.com/sizlo/raspberry-pi-spring-boot-spike/raw/main/deployment-resources/jdk-17-ga_gcc-10.1_glibc-2.28_binutils-2.31_Buster.tar.gz
  tar zxf jdk-17-ga_gcc-10.1_glibc-2.28_binutils-2.31_Buster.tar.gz
  echo "jdk installed"
fi

# Install jq if required
if ! command -v jq &> /dev/null
then
    echo "Installing jq"
    sudo apt install jq -y
    echo "jq installed"
fi

echo "Downloading latest jar"
RELEASE_JSON="$(curl https://api.github.com/repos/sizlo/raspberry-pi-spring-boot-spike/releases/latest)"
FILENAME="$(echo $RELEASE_JSON | jq -r '.assets[0].name')"
URL="$(echo $RELEASE_JSON | jq -r '.assets[0].browser_download_url')"
wget $URL
echo "jar downloaded"

echo "Running jar"
jdk-17-ga/bin/java -jar "$FILENAME"