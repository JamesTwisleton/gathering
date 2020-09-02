#!/bin/bash
pkill -f .*javaApp
git reset --hard
git pull
mvn clean package
screen -S javaApp -d -m java -jar --enable-preview target/gathering-0.0.1-SNAPSHOT.jar