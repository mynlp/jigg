#!/bin/bash

set -e

docker build -t jigg/jigg:syntaxnet -f dockers/syntaxnet/Dockerfile .
