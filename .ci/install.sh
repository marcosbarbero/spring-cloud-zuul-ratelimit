#!/usr/bin/env bash

echo "[START GPG] Setup Signing Key"

if [ "$TRAVIS_BRANCH" = "master" ] && [ "$TRAVIS_PULL_REQUEST" = "false" ] && [[ "$TRAVIS_COMMIT_MESSAGE" == *"[ci deploy]"* ]];
then
  echo $GPG_SECRET_KEYS | base64 --decode | $GPG_EXECUTABLE --import
  echo $GPG_OWNERTRUST | base64 --decode | $GPG_EXECUTABLE --import-ownertrust
fi

echo "[END GPG] Setup Signing Key"