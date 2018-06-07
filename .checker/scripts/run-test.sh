#!/bin/bash

set -e

# run an unit test for the files under the directory `.checker/tests`.
python -m unittest discover -s .checker/tests/
