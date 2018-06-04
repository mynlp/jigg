#!/bin/bash

set -e

# argument
jar_file_name=${1}
text_dir_name=${2}
input_file_name=${3}

output_file_name="${text_dir_name}/${input_file_name}.xml"

target_file_name="${text_dir_name}/ssplit-target.xml"

java -cp ${jar_file_name} jigg.pipeline.Pipeline \
     -annotators "corenlp[tokenize,ssplit]" \
     -file "${text_dir_name}/${input_file_name}" \
     -output ${output_file_name}

diff -qs ${output_file_name} ${target_file_name}


