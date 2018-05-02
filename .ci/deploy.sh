#!/usr/bin/env bash

echo "Branch: $TRAVIS_BRANCH"
echo "Pull Request? $TRAVIS_PULL_REQUEST"
echo "Commit Message: $TRAVIS_COMMIT_MESSAGE"

if [ "$TRAVIS_BRANCH" = "master" ] && [ "$TRAVIS_PULL_REQUEST" = "false" ] && [[ "$TRAVIS_COMMIT_MESSAGE" == *"[ci deploy]"* ]]; then
  mvn --settings .ci/settings.xml clean deploy -DskipTests -Pdeploy-parent -N
  mvn --settings .ci/settings.xml -f spring-cloud-zuul-ratelimit-dependencies/pom.xml clean deploy -DskipTests -Pdeploy
  mvn --settings .ci/settings.xml -f spring-cloud-starter-zuul-ratelimit/pom.xml clean deploy -DskipTests -Pdeploy
  mvn --settings .ci/settings.xml -f spring-cloud-zuul-ratelimit-core/pom.xml clean deploy -DskipTests -Pdeploy

  echo "New version released"
fi

echo "Build Finished"