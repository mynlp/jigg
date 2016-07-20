#!/bin/sh

jar=`dirname $0`/../jar
version=0.6.1

modelurl="https://github.com/mynlp/jigg/releases/download/v-${version}/jigg-${version}-models.jar"

wget $modelurl -P $jar
