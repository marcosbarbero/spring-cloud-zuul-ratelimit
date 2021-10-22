#!/usr/bin/env bash

echo "[START GPG] Setup Signing Key"

if [ "$TRAVIS_BRANCH" = "master" ] && [ "$TRAVIS_PULL_REQUEST" = "false" ] && [[ "$TRAVIS_COMMIT_MESSAGE" == *"[ci deploy]"* ]]; then
  openssl aes-256-cbc -K $encrypted_SOME_key -iv $encrypted_SOME_iv -in .ci/signingkey.asc.enc -out .ci/signingkey.asc -d
  gpg --fast-import .ci/signingkey.asc
if

echo "[END GPG] Setup Signing Key"