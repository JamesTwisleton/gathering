#!/bin/bash
pkill -f .*reactApp
cd js
git pull -q -f
npm-install-changed >/dev/null
screen -S reactApp -d -m npm start