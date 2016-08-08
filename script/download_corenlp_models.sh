#!/bin/sh

home=`dirname $0`/..

modelurl="http://nlp.stanford.edu/software/stanford-english-corenlp-2016-01-10-models.jar"

wget $modelurl -P $home
