#!/bin/bash

set -e

home_dir=`pwd ./`

pip install cython numpy
pip install depccg

depccg_en download
depccg_ja download

# en_model_url=http://cl.naist.jp/~masashi-y/resources/depccg/en_hf_tri.tar.gz
# ja_model_url=http://cl.naist.jp/~masashi-y/resources/depccg/ja_hf_ccgbank.tar.gz
# en_model=en_hf_tri.tar.gz
# ja_model=ja_hf_ccgbank.tar.gz

# model_dir="depccg/models"
# src_dir="depccg/src"

# # Install cython & chainer.
# pip install -U pip cython
# pip install chainer
# pip install scrapy

# # Git clone the depccg repository
# git clone https://github.com/masashi-y/depccg.git

# # download model file.
# wget ${en_model_url}
# wget ${ja_model_url}

# # make directory saved model file
# mkdir ${model_dir}
# mv ${en_model} ${ja_model} ${model_dir}

# # compile
# # A default g++ version is 4.8 in Ubuntu 14.04.
# # In depccg compile, it requires the version >= 4.9.
# export CC=g++-4.9
# cd ${home_dir}"/"${src_dir}
# python setup.py build_ext --inplace

# ln -s depccg*.so depccg.so

# # unpack model files.
# cd ${home_dir}"/"${model_dir}
# tar -zxvf ${en_model}
# tar -zxvf ${ja_model}

# cd ${home_dir}
