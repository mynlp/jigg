#!/bin/bash

set -e

# run an unit test for the files under the directory `.checker/tests`.
python3 -m unittest discover -s .checker/tests/
