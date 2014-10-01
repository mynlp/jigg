#!/bin/sh

model=`dirname $0`/../model

mkdir -p $model

wget http://kmcs.nii.ac.jp/~noji/transccg/jaccg-0.1-beam64.ser.gz -P $model

wget http://kmcs.nii.ac.jp/~noji/transccg/jaccg-0.1-beam128.ser.gz -P $model
