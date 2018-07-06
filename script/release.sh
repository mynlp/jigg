#!/bin/sh

# Usage: ./script/release.sh <version-number> (e.g., 0.7.2)

version=$1
corenlp_url='http://nlp.stanford.edu/software/stanford-corenlp-full-2018-02-27.zip'
corenlp_model='stanford-corenlp-3.9.1-models.jar'
jigg_url='git@github.com:mynlp/jigg.git'

corenlp_zip=${corenlp_url##*/}
corenlp_dir=${corenlp_zip%.*}

if [[ ! -e jigg-${version} ]]; then mkdir jigg-${version}; fi
cd jigg-${version}

# get jigg, if needed
if [[ ! -e jigg ]]; then
    git clone $jigg_url
fi

# add corenlp model
if [[ ! -e ${corenlp_dir} ]]; then
    wget ${corenlp_url} -O ${corenlp_zip}
    unzip ${corenlp_zip}
    mv ${corenlp_dir}/${corenlp_model} jigg
fi

# add assembled jigg
if [[ ! -e jigg/jigg-$1.jar ]]; then
    cd jigg
    ./bin/sbt assembly
    mv target/jigg-assembly-$1.jar jigg-$1.jar
    ./bin/sbt clean
    cd ../
fi

for f in 'src/test' '.checker' '.git' 'project' 'target'; do
    if [[ -e jigg/$f ]]; then
        rm -rf jigg/$f
    fi
done

if [[ -e jigg/.git ]]; then
    rm -rf jigg/.git
fi

# if [[ -e jigg/src/test ]]; then
#     rm -rf jigg/src/test
# fi

# if [[ -e jigg/.checker ]]; then rm -rf jigg/.checker; fi

# if [[ -e jigg/project ]]; then rm -rf jigg/project; fi
# if [[ -e jigg/target ]]; then rm -rf jigg/target; fi

# add jigg models (berkeley parser model inside)
if [[ ! -e jigg/jigg-models.jar ]]; then
    cd jigg
    wget https://github.com/mynlp/jigg-models/raw/master/jigg-models.jar
    cd ../
fi

mv jigg jigg-${version}
zip -r jigg-${version}.zip jigg-${version}
