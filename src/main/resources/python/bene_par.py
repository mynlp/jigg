
from __future__ import print_function, unicode_literals
import sys

import benepar

# In Python2, wrap sys.stdin and sys.stdout to work with unicode.
if sys.version_info[0] < 3:
    import codecs
    import locale
    encoding = locale.getpreferredencoding()
    sys.stdin = codecs.getreader(encoding)(sys.stdin)
    sys.stdout = codecs.getwriter(encoding)(sys.stdout)

if sys.version_info.major == 3:
    raw_input = input

model = sys.argv[1] # maybe "benepar_en"

parser = benepar.Parser(model)

def parse(tokens, tags):
    sentence = list(zip(tokens, tags))
    parse_raw, tags_raw, sentence = next(parser._batched_parsed_raw([(tokens, sentence)]))
    tree = parser._make_nltk_tree(sentence, tags_raw, *parse_raw)
    return tree

while True:
    tokens = raw_input()
    tags = raw_input()

    tokens = tokens.split(' ')
    tags = tags.split(' ')

    tree = parse(tokens, tags)
    print(tree)
    print("END")
