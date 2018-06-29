#!/bin/bash

set -e

source .checker/scripts/set-env.sh

# run an unit test for the files under the directory `.checker/tests/${ANNOTATORS}`.
python3 -m unittest discover -s .checker/tests/${ANNOTATORS}
