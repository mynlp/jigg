
from __future__ import print_function, unicode_literals

"""An interface depccg called from DepCCGAnnotator.scala

Usage: python depccg.py srcdir model nbest
"""

import sys

if sys.version_info.major == 3:
    raw_input = input

def print_xml(trees):
    print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    print("<?xml-stylesheet type=\"text/xsl\" href=\"candc.xml\"?>")
    print("<candc>")
    for i, kbest in enumerate(trees, 1):
        print("<ccgs sentence=\"{}\">".format(i))
        for j, (t, _) in enumerate(kbest, 1):
            print("<ccg sentence=\"{}\" id=\"{}\">".format(i, j))
            print(t.xml)
            print("</ccg>")
        print("</ccgs>")
    print("</candc>")

srcdir = sys.argv[1]
sys.path.insert(0, srcdir)
from depccg import PyAStarParser, PyJaAStarParser

model = sys.argv[2]
nbest = int(sys.argv[3])
lang = sys.argv[4]

if lang == 'en':
    parser = PyAStarParser(model, nbest=nbest, loglevel=3)
else:
    parser = PyJaAStarParser(model, nbest=nbest, loglevel=3)

while True:
    inputs = []
    while True:
        line = raw_input()
        if line == '####EOD####': break
        inputs.append(line)

    parses = parser.parse_doc(inputs)
    print_xml(parses)
    print("END")
