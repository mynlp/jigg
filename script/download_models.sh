#!/bin/sh

home=`dirname $0`/..
version=0.6.1

modelurl="https://github.com/mynlp/jigg/releases/download/v-${version}/jigg-${version}-models.jar"

wget $modelurl -P $home
