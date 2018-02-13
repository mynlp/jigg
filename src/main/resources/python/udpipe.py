
from __future__ import print_function, unicode_literals
import sys

from ufal.udpipe import Model, Pipeline, ProcessingError

# In Python2, wrap sys.stdin and sys.stdout to work with unicode.
if sys.version_info[0] < 3:
    import codecs
    import locale
    encoding = locale.getpreferredencoding()
    sys.stdin = codecs.getreader(encoding)(sys.stdin)
    sys.stdout = codecs.getwriter(encoding)(sys.stdout)

if sys.version_info.major == 3:
    raw_input = input

# To reduce the overhead we divide the patterns of a possible pipeline into 3 cases.
_MODE_ = ['all', 'tok|pos', 'pos|par', 'tok', 'pos', 'par']

model = sys.argv[1]
mode = sys.argv[2] # one of _MODE_

model = Model.load(model)

if mode == 'all' or mode.find('tok') >= 0: input_format = 'tokenize'
else: input_format = 'conllu'
output_format = 'conllu'

if mode == 'all' or mode.find('pos') >= 0: pos = Pipeline.DEFAULT
else: pos = Pipeline.NONE

if mode == 'all' or mode.find('par') >= 0: parse = Pipeline.DEFAULT
else: parse = Pipeline.NONE

pipeline = Pipeline(
    model, input_format, pos, parse, output_format)
error = ProcessingError()

while True:
    inputs = []
    while True:
        line = raw_input()
        if line == '####EOD####': break
        inputs.append(line)

    result = pipeline.process('\n'.join(inputs), error)
    print(result)
    print('END')
