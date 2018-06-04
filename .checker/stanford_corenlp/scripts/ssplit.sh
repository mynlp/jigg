#!/bin/bash

set -e

STFNLP_VERSION="3.8.0"

# argument
jar_file_name=${1}
text_dir_name=${2}
input_file_name=${3}

output_file_name="${text_dir_name}/${input_file_name}.out"

target_file_name="${text_dir_name}/ssplit-target.txt"

java -cp ${jar_file_name} edu.stanford.nlp.pipeline.StanfordCoreNLP \
     -annotators tokenize,ssplit \
     -file "${text_dir_name}/${input_file_name}" \
     -outputDirectory ${text_dir_name}

diff -qs ${output_file_name} ${target_file_name}

rm ${output_file_name}

