#!/bin/bash

if [ ${ANNOTATORS} == "udpipe" ];then
    echo "Install UDPIPE"
    ./.checker/scripts/install-udpipe.sh
elif [ ${ANNOTATORS} == "depccg" ];then
    echo "Install DEPCCG"
    ./.checker/scripts/install-depccg.sh    
elif [ ${ANNOTATORS} == "mecab" ];then
    echo "Install MECAB"
    ./.checker/scripts/install-mecab.sh
elif [ ${ANNOTATORS} == "cabocha" ];then
    echo "Install CABOCHA"
    ./.checker/scripts/install-mecab.sh
    ./.checker/scripts/install-crf.sh
    ./.checker/scripts/install-cabocha.sh
elif [ ${ANNOTATORS} == "juman" ];then
    echo "Install JUMAN"
    ./.checker/scripts/install-juman.sh
elif [ ${ANNOTATORS} == "knp" ];then
    echo "Install KNP"
    ./.checker/scripts/install-juman.sh
    ./.checker/scripts/install-knp.sh
elif [ ${ANNOTATORS} == "corenlp" ];then
    echo "Install CORENLP"
    ./.checker/scripts/install-jar.sh
elif [ ${ANOTATORS} == "benepar" ];then
    echo "Install BENEPAR"
    ./.checker/scripts/install-benepar.sh
fi
