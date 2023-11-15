#!/usr/bin/env bash

rm -rf target

./mvnw -DskipTests   package

java -XX:CRaCCheckpointTo=./crac-files  -jar target/crac-0.0.1-SNAPSHOT.jar
