#!/bin/bash

set -e

source ./.checker/scripts/set-env.sh

home_dir=`pwd ./`

url="https://drive.google.com/uc?export=download"
file_id="id=0B4y35FiV1wh7SDd1Q1dUQkZQaUU"
file=cabocha-0.69.tar.bz2
dir=cabocha-0.69

# download
curl -sc /tmp/cookie ${url}"&"${file_id} > /dev/null
CODE=`awk '/__warning__/ {print $NF}' /tmp/cookie`

if [ -n ${CODE} ];then
    curl -Lb /tmp/cookie ${url}"&"${file_id} -o ${file}
else
    curl -Lb /tmp/cookie ${url}"&confirm="${CODE}"&"${file_id} -o ${file}
fi

# unpack
tar -jxvf ${file}

# compile
cd ${home_dir}"/"${dir}
./configure --with-charset=UTF8
make
make check
sudo make install

cd ${home_dir}
