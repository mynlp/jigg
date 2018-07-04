#!/bin/bash

set -e

source ./.checker/scripts/set-env.sh

home_dir=`pwd ./`

url="https://github.com/taku910/cabocha/archive/master.zip"
file=master.zip
dir=cabocha-master

# download
wget ${url}

# unpack
unzip ${file}

# compile
cd ${home_dir}"/"${dir}
./autogen.sh
./configure --with-charset=UTF8
make
make check
sudo make install

cd ${home_dir}
