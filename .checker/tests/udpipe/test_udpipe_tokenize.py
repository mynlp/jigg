import sys
sys.path.append(".checker/tests")

from basetest import BaseTest


class TestUDpipeTokenize(BaseTest):

    def setUp(self):
        self.input_text = "Stanford University is located in California. It is a great university, founded in 1891."

        self.expected_text = r"""<?xml version='1.0' encoding='UTF-8'?>
<root>
  <document id="d0">
    <sentences>
      <sentence characterOffsetEnd="45" characterOffsetBegin="0" id="s0">
        Stanford University is located in California.
        <tokens annotators="udpipe">
          <token offsetEnd="8" offsetBegin="0" form="Stanford" id="t0"/>
          <token offsetEnd="19" offsetBegin="9" form="University" id="t1"/>
          <token offsetEnd="22" offsetBegin="20" form="is" id="t2"/>
          <token offsetEnd="30" offsetBegin="23" form="located" id="t3"/>
          <token offsetEnd="33" offsetBegin="31" form="in" id="t4"/>
          <token offsetEnd="44" offsetBegin="34" form="California" id="t5"/>
          <token offsetEnd="45" offsetBegin="44" form="." id="t6"/>
        </tokens>
      </sentence>
      <sentence characterOffsetEnd="88" characterOffsetBegin="46" id="s1">
        It is a great university, founded in 1891.
        <tokens annotators="udpipe">
          <token offsetEnd="48" offsetBegin="46" form="It" id="t7"/>
          <token offsetEnd="51" offsetBegin="49" form="is" id="t8"/>
          <token offsetEnd="53" offsetBegin="52" form="a" id="t9"/>
          <token offsetEnd="59" offsetBegin="54" form="great" id="t10"/>
          <token offsetEnd="70" offsetBegin="60" form="university" id="t11"/>
          <token offsetEnd="71" offsetBegin="70" form="," id="t12"/>
          <token offsetEnd="79" offsetBegin="72" form="founded" id="t13"/>
          <token offsetEnd="82" offsetBegin="80" form="in" id="t14"/>
          <token offsetEnd="87" offsetBegin="83" form="1891" id="t15"/>
          <token offsetEnd="88" offsetBegin="87" form="." id="t16"/>
        </tokens>
      </sentence>
    </sentences>
  </document>
</root>"""

        self.exe = 'runMain jigg.pipeline.Pipeline ' \
                   + '-annotators udpipe[tokenize] ' \
                   + '-udpipe.model udpipe-ud-model/english-ud-2.0-170801.udpipe '

    def test_udpipe_tokenize(self):
        self.check_equal(self.exe, self.input_text, self.expected_text)
