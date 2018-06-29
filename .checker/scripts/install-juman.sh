#!/bin/bash

set -e

home_dir=`pwd ./`

url=http://nlp.ist.i.kyoto-u.ac.jp/nl-resource/juman/juman-7.01.tar.bz2
file=juman-7.01.tar.bz2
dir=juman-7.01

# download
wget ${url}

# unpack bz2 file
tar -jxvf ${file}

# build
cd ${dir}
./configure
make
sudo make install

cd ${home_dir}
