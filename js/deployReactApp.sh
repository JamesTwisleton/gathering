#!/bin/bash
pkill -f .*reactApp
git reset --hard
git pull
npm-install-changed >/dev/null
screen -S reactApp -d -m npm start