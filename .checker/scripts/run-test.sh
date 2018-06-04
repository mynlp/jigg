#!/bin/bash

set -e

source ./.checker/scripts/set-env.sh

# stanford-corenlp
./.checker/stanford_corenlp/scripts/check_all.sh

# jigg & stanford-corenlp
./.checker/jigg/scripts/check_all.sh

