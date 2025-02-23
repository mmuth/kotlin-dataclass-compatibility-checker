#!/bin/zsh

echo "Validating samples, please load the latest validator JAR to this location and install buf (e.g. brew install buf)."

buf breaking ./inputfile/car.proto --against ./against/car.proto

java -jar ./validator-latest.jar --input ./inputfile/Car.kt --against ./against/Car.kt
