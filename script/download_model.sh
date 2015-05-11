#!/bin/sh

jar=`dirname $0`/../jar

modelurl="http://downloads.sourceforge.net/project/jigg-models/ccg-models-0.4.jar"

wget $modelurl -P $jar
