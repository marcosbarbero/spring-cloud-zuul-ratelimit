#!/usr/bin/env bash

if [[ $TRAVIS_COMMIT_MESSAGE == *"[ci deploy]"* ]]; then
  mvn --settings ./settings.xml -B -f ../pom.xml clean deploy -Pdeploy-parent -N
  mvn --settings ./settings.xml -B -f ../pom.xml clean deploy -Pdeploy -N
fi