#!/bin/bash

set -e

pip install ufal.udpipe

# model download
curl --remote-name-all https://lindat.mff.cuni.cz/repository/xmlui/bitstream/handle/11234/1-2364/udpipe-ud-2.0-170801.zip

# unpack
unzip udpipe-ud-2.0-170801.zip

# rename model directory 
mv udpipe-ud-2.0-170801 udpipe-ud-model
