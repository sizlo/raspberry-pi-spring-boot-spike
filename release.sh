#!/bin/bash

set -e

gitCommitAndPush () {
  echo "Committing and pushing changes"
  git add .
  git commit -m "$1"
  git push
}

getVersionFromLatestGitRelease () {
  gh release view 2> /dev/null | grep "^tag:" | cut -f 2 -w | sed "s/v//"
}

getVersionFromBuildGradle () {
  grep 'version\s*=\s*"\d*\.\d*\.\d*.*"' build.gradle.kts | cut -f 2 -d '"'
}

upgradeVersion () {
  CURRENT_VERSION=$(getVersionFromBuildGradle)
  NEW_VERSION=$1

  if [[ $CURRENT_VERSION != $NEW_VERSION ]]
  then
    echo "Upgrading version in build files from $CURRENT_VERSION to $NEW_VERSION"
    sed -i "" "s/version[ \t]*=[ \t]*\"$CURRENT_VERSION\"/version = \"$NEW_VERSION\"/g" build.gradle.kts
    gitCommitAndPush "Upgrade version from $CURRENT_VERSION to $NEW_VERSION"
  fi
}

RELEASE_TYPE=$1

CURRENT_BRANCH=$(git branch --show-current)
if [[ $CURRENT_BRANCH != master && $CURRENT_BRANCH != main ]]
then
  echo The release script must be run from the main branch
  exit 1
fi

if [[ $(git status --porcelain) ]]
then
  echo Your local git repo has changes, releases should be done on clean repos
  exit 1
fi

if [[ $RELEASE_TYPE != major && $RELEASE_TYPE != minor && $RELEASE_TYPE != patch ]]
then
  echo Please provide the release type: major minor or patch
  exit 1
fi

STARTING_VERSION=$(getVersionFromLatestGitRelease)

if [[ -z "${STARTING_VERSION}" ]]
then
  STARTING_VERSION=0.0.0
  echo "No github release found, starting from version 0.0.1"
else
  echo "Found version $STARTING_VERSION from latest git release"
fi

STARTING_MAJOR=$(echo $STARTING_VERSION | cut -f 1 -d .)
STARTING_MINOR=$(echo $STARTING_VERSION | cut -f 2 -d .)
STARTING_PATCH=$(echo $STARTING_VERSION | cut -f 3 -d .)

if [[ $RELEASE_TYPE == major ]]
then
  RELEASE_MAJOR=$(($STARTING_MAJOR + 1))
  RELEASE_MINOR=0
  RELEASE_PATCH=0
elif [[ $RELEASE_TYPE == minor ]]
then
  RELEASE_MAJOR=$STARTING_MAJOR
  RELEASE_MINOR=$(($STARTING_MINOR + 1))
  RELEASE_PATCH=0
else
  RELEASE_MAJOR=$STARTING_MAJOR
  RELEASE_MINOR=$STARTING_MINOR
  RELEASE_PATCH=$((STARTING_PATCH + 1))
fi

RELEASE_VERSION="$RELEASE_MAJOR.$RELEASE_MINOR.$RELEASE_PATCH"

echo "Releasing version $RELEASE_VERSION"

if [[ $RELEASE_VERSION != $STARTING_VERSION ]]
then
  upgradeVersion $RELEASE_VERSION
fi

JAR_PATH="build/libs/raspberrypispringbootspike-$RELEASE_VERSION.jar"

echo "Building jar"
./gradlew clean build
JAR_SHA1SUM=$(sha1sum $JAR_PATH | cut -f 1 -d " ")

echo "Creating github release $RELEASE_VERSION"
gh release create \
  "v$RELEASE_VERSION" \
  $JAR_PATH \
  --title $RELEASE_VERSION \
  --notes "jar sha1sum: $JAR_SHA1SUM" \
  --latest

SNAPSHOT_VERSION="$RELEASE_MAJOR.$RELEASE_MINOR.$((RELEASE_PATCH + 1))-SNAPSHOT"
upgradeVersion $SNAPSHOT_VERSION


