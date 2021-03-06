import sys
sys.path.append(".checker/tests")

from basetest import BaseTest


class TestTokenize(BaseTest):

    def setUp(self):

        self.input_text = "Stanford University is located in California. It is a great university, founded in 1891."

        self.expected_text = """<?xml version='1.0' encoding='UTF-8'?>
<root>
  <document id="d0">
    <sentences>
      <sentence characterOffsetEnd="88" characterOffsetBegin="0" id="s0">
        <tokens annotators="corenlp">
          <token characterOffsetEnd="8" characterOffsetBegin="0" form="Stanford" id="t0"/>
          <token characterOffsetEnd="19" characterOffsetBegin="9" form="University" id="t1"/>
          <token characterOffsetEnd="22" characterOffsetBegin="20" form="is" id="t2"/>
          <token characterOffsetEnd="30" characterOffsetBegin="23" form="located" id="t3"/>
          <token characterOffsetEnd="33" characterOffsetBegin="31" form="in" id="t4"/>
          <token characterOffsetEnd="44" characterOffsetBegin="34" form="California" id="t5"/>
          <token characterOffsetEnd="45" characterOffsetBegin="44" form="." id="t6"/>
          <token characterOffsetEnd="48" characterOffsetBegin="46" form="It" id="t7"/>
          <token characterOffsetEnd="51" characterOffsetBegin="49" form="is" id="t8"/>
          <token characterOffsetEnd="53" characterOffsetBegin="52" form="a" id="t9"/>
          <token characterOffsetEnd="59" characterOffsetBegin="54" form="great" id="t10"/>
          <token characterOffsetEnd="70" characterOffsetBegin="60" form="university" id="t11"/>
          <token characterOffsetEnd="71" characterOffsetBegin="70" form="," id="t12"/>
          <token characterOffsetEnd="79" characterOffsetBegin="72" form="founded" id="t13"/>
          <token characterOffsetEnd="82" characterOffsetBegin="80" form="in" id="t14"/>
          <token characterOffsetEnd="87" characterOffsetBegin="83" form="1891" id="t15"/>
          <token characterOffsetEnd="88" characterOffsetBegin="87" form="." id="t16"/>
        </tokens>
      </sentence>
    </sentences>
  </document>
</root>"""

        self.exe = 'runMain jigg.pipeline.Pipeline -annotators corenlp[tokenize]'

    def test_tokenize(self):
        self.check_equal(self.exe, self.input_text, self.expected_text)
