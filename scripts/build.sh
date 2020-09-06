#!/bin/bash

mvn package -Dengine.version=$1 -Dengine.builddate=`date +%Y-%m-%d`