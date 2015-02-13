#!/bin/bash

set -e

cd ../ml_java && mvn package -DskipTests
cp target/nto-1.0.jar ../webapp_scala/lib/nto-1.0.jar
cd ../webapp_scala
sbt

