#!/usr/bin/env python

from pyjigg import Pipeline
import xml.etree.ElementTree as ET
import json

'''This is for repeated annotations of different texts with the same annotation setting.

Before using this, users must start the PipelineServer in a command line, e.g.:
 $ java -Xmx4g -cp "jigg-xxx" jigg.pipeline.PipelineServer \
   -annotators "corenlp[tokenize,ssplit],berkeleyparser"
'''

if __name__ == '__main__':
    pipeline = Pipeline('http://localhost:8080')

    text1 = """This is the first sentence. This is the second sentence."""

    text2 = """This is the third sentence. This is the forth sentence."""

    output1 = pipeline.annotateXML(text1)
    print ET.tostring(output1)

    output2 = pipeline.annotateJSON(text2)
    print json.dumps(output2, indent=4)
