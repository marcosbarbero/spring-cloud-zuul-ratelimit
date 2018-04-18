#!/usr/bin/env bash

if [[ $TRAVIS_COMMIT_MESSAGE == *"[ci deploy]"* ]]; then
  mvn --settings .ci/settings.xml clean deploy -Pdeploy-parent -N
  mvn --settings .ci/settings.xml clean deploy -Pdeploy -N
fi