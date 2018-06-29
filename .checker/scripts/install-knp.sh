#!/bin/bash

set -e

home_dir=`pwd ./`

url=http://nlp.ist.i.kyoto-u.ac.jp/nl-resource/knp/knp-4.19.tar.bz2
file=knp-4.19.tar.bz2
dir=knp-4.19

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
