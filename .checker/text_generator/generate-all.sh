#!/bin/bash

source ./.checker/scripts/set-env.sh

./.checker/text_generator/stanford-corenlp.sh

./.checker/text_generator/jigg.sh
