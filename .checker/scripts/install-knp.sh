#!/bin/bash

set -e

docker build -t jigg/jigg:knp -f dockers/knp/Dockerfile .
