#!/bin/bash

set -e

pip install cython numpy
pip install benepar[cpu]

python -c 'import benepar; benepar.download("benepar_en2")'
