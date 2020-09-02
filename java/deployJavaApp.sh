#!/bin/bash
pkill -f .*javaApp
cd java
git pull -q -f
mvn clean package
screen -S javaApp -d -m java -jar --enable-preview target/gathering-0.0.1-SNAPSHOT.jar