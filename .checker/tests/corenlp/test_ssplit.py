import sys
sys.path.append(".checker/tests")

from basetest import BaseTest


class TestSsplit(BaseTest):

    def setUp(self):

        self.input_text = "Stanford University is located in California. It is a great university, founded in 1891."

        self.expected_text = """<?xml version='1.0' encoding='UTF-8'?>
<root>
  <document id="d0">
    <sentences>
      <sentence characterOffsetEnd="45" characterOffsetBegin="0" id="s0">
        Stanford University is located in California.
        <tokens annotators="corenlp">
          <token form="Stanford" id="t0" characterOffsetBegin="0" characterOffsetEnd="8"/>
          <token form="University" id="t1" characterOffsetBegin="9" characterOffsetEnd="19"/>
          <token form="is" id="t2" characterOffsetBegin="20" characterOffsetEnd="22"/>
          <token form="located" id="t3" characterOffsetBegin="23" characterOffsetEnd="30"/>
          <token form="in" id="t4" characterOffsetBegin="31" characterOffsetEnd="33"/>
          <token form="California" id="t5" characterOffsetBegin="34" characterOffsetEnd="44"/>
          <token form="." id="t6" characterOffsetBegin="44" characterOffsetEnd="45"/>
        </tokens>
      </sentence>
      <sentence characterOffsetEnd="88" characterOffsetBegin="46" id="s1">
        It is a great university, founded in 1891.
        <tokens annotators="corenlp">
          <token form="It" id="t7" characterOffsetBegin="0" characterOffsetEnd="2"/>
          <token form="is" id="t8" characterOffsetBegin="3" characterOffsetEnd="5"/>
          <token form="a" id="t9" characterOffsetBegin="6" characterOffsetEnd="7"/>
          <token form="great" id="t10" characterOffsetBegin="8" characterOffsetEnd="13"/>
          <token form="university" id="t11" characterOffsetBegin="14" characterOffsetEnd="24"/>
          <token form="," id="t12" characterOffsetBegin="24" characterOffsetEnd="25"/>
          <token form="founded" id="t13" characterOffsetBegin="26" characterOffsetEnd="33"/>
          <token form="in" id="t14" characterOffsetBegin="34" characterOffsetEnd="36"/>
          <token form="1891" id="t15" characterOffsetBegin="37" characterOffsetEnd="41"/>
          <token form="." id="t16" characterOffsetBegin="41" characterOffsetEnd="42"/>
        </tokens>
      </sentence>
    </sentences>
  </document>
</root>"""

        self.exe = 'runMain jigg.pipeline.Pipeline -annotators corenlp[tokenize,ssplit]'

    def test_ssplit(self):
        self.check_equal(self.exe, self.input_text, self.expected_text)
