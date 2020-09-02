#!/bin/bash
pkill -f .*javaApp
cd git/gathering
git reset --hard
git pull
mvn clean package
screen -S javaApp -d -m java -jar --enable-preview target/gathering-0.0.1-SNAPSHOT.jar