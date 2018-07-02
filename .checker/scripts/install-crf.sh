#!/bin/bash

set -e

home_dir=`pwd ./`

# To get file id, you singup google account.
url="https://drive.google.com/uc?export=view&id=0B4y35FiV1wh7QVR6VXJ5dWExSTQ"
file=CRF++-0.58.tar.gz
dir=CRF++-0.58

wget ${url} -O ${file}

tar -zxvf ${file}

cd ${home_dir}"/"${dir}
./configure
make
sudo make install

cd ${home_dir}
