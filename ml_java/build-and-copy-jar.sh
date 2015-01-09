#!/bin/bash

set -e

cd ../ml_java && mvn package -DskipTests
mv target/nto-0.1-SNAPSHOT.jar ../webapp_scala/lib/nto-0.1-SNAPSHOT.jar
cd ../webapp_scala
sbt

