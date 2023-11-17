#!/usr/bin/env bash

./mvnw -DskipTests clean package

docker build . -t crac:latest

docker run   -it --privileged --rm -p 8080:8080 --name crac crac