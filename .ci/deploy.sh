#!/usr/bin/env bash

mvn -pl '!spring-cloud-zuul-ratelimit-coverage' clean deploy -Pdeploy-parent -fn