#!/usr/bin/env sh

echo "[START GPG] Setup Signing Key"

if [ "$TRAVIS_BRANCH" = "master" ] && [ "$TRAVIS_PULL_REQUEST" = "false" ] && [ "$TRAVIS_COMMIT_MESSAGE" == *"[ci deploy]"* ];
then
  - openssl aes-256-cbc -K $encrypted_01edcdfe89aa_key -iv $encrypted_01edcdfe89aa_iv -in .ci/secret.asc.enc -out .ci/secret.asc -d
fi

echo "[END GPG] Setup Signing Key"